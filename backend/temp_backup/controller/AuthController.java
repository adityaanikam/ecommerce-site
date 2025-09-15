package com.ecommerce.controller;

import com.ecommerce.dto.AuthDto;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final EmailService emailService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest loginRequest, 
                                                     HttpServletRequest request) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        AuthDto.AuthResponse response = authService.login(loginRequest, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(@Valid @RequestBody AuthDto.RegisterRequest registerRequest,
                                                        HttpServletRequest request) {
        log.info("Registration request received for email: {}", registerRequest.getEmail());
        AuthDto.AuthResponse response = authService.register(registerRequest, request);
        
        // Send welcome email
        emailService.sendWelcomeEmail(registerRequest.getEmail(), registerRequest.getFirstName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthDto.AuthResponse> refreshToken(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        log.info("Refresh token request received");
        AuthDto.AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<AuthDto.MessageResponse> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received");
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        authService.logout(token);
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Logged out successfully")
                .build());
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<AuthDto.MessageResponse> logoutAllDevices(Authentication authentication) {
        log.info("Logout all devices request received");
        String username = authentication.getName();
        authService.logoutAllDevices(username);
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Logged out from all devices successfully")
                .build());
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<AuthDto.MessageResponse> changePassword(@Valid @RequestBody AuthDto.PasswordChangeRequest passwordChangeRequest,
                                                Authentication authentication) {
        log.info("Change password request received");
        String username = authentication.getName();
        authService.changePassword(username, passwordChangeRequest);
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Password changed successfully")
                .build());
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthDto.MessageResponse> forgotPassword(@Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        // TODO: Implement forgot password logic
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Password reset instructions sent to your email")
                .build());
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<AuthDto.MessageResponse> resetPassword(@Valid @RequestBody AuthDto.ResetPasswordRequest request) {
        log.info("Reset password request received");
        // TODO: Implement reset password logic
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Password reset successfully")
                .build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<AuthDto.AuthResponse> getCurrentUser(Authentication authentication) {
        log.info("Get current user request received");
        // TODO: Implement get current user logic
        return ResponseEntity.ok(AuthDto.AuthResponse.builder()
                .email(authentication.getName())
                .build());
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<AuthDto.MessageResponse> verifyEmail(@Valid @RequestBody AuthDto.EmailVerificationRequest request) {
        log.info("Email verification request received");
        // TODO: Implement email verification logic
        return ResponseEntity.ok(AuthDto.MessageResponse.builder()
                .message("Email verified successfully")
                .build());
    }
}
