package com.ecommerce.service;

import com.ecommerce.dto.AuthDto;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitingFilter rateLimitingFilter;
    
    public AuthDto.AuthResponse login(AuthDto.LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            
            // Reset login attempts on successful login
            String clientIp = getClientIpAddress(request);
            rateLimitingFilter.resetLoginAttempts(clientIp);
            
            log.info("Login successful for user: {}", user.getEmail());
            
            return AuthDto.AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .imageUrl(user.getImageUrl())
                    .roles(user.getRoles())
                    .provider(user.getProvider())
                    .build();
                    
        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest registerRequest, HttpServletRequest request) {
        log.info("Attempting registration for email: {}", registerRequest.getEmail());
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.USER);
        
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phone(registerRequest.getPhone())
                .roles(roles)
                .provider(User.AuthProvider.LOCAL)
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        log.info("Registration successful for user: {}", savedUser.getEmail());
        
        return AuthDto.AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .imageUrl(savedUser.getImageUrl())
                .roles(savedUser.getRoles())
                .provider(savedUser.getProvider())
                .build();
    }
    
    public AuthDto.AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String newToken = jwtTokenProvider.refreshToken(refreshToken);
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            
            User user = userRepository.findByEmailAndIsActiveTrue(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return AuthDto.AuthResponse.builder()
                    .token(newToken)
                    .refreshToken(refreshToken)
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .imageUrl(user.getImageUrl())
                    .roles(user.getRoles())
                    .provider(user.getProvider())
                    .build();
        }
        
        throw new RuntimeException("Invalid refresh token");
    }
    
    public void logout(String token) {
        log.info("Logging out user");
        
        // Blacklist the token
        jwtTokenProvider.blacklistToken(token);
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
    
    public void logoutAllDevices(String username) {
        log.info("Logging out all devices for user: {}", username);
        
        // Revoke all tokens for user
        jwtTokenProvider.revokeAllUserTokens(username);
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
    
    public void changePassword(String username, AuthDto.PasswordChangeRequest passwordChangeRequest) {
        log.info("Changing password for user: {}", username);
        
        User user = userRepository.findByEmailAndIsActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password confirmation
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
        
        // Revoke all existing tokens
        jwtTokenProvider.revokeAllUserTokens(username);
        
        log.info("Password changed successfully for user: {}", username);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}