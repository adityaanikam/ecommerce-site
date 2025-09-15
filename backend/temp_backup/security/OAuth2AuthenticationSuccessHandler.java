package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        
        log.info("OAuth2 authentication successful for user: {}", email);
        
        // Check if user exists, if not create new user
        User user = getOrCreateUser(email, name, picture, oAuth2User);
        
        // Generate JWT token
        String token = tokenProvider.generateTokenFromUsername(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        // Create response data
        Map<String, Object> responseData = Map.of(
            "token", token,
            "refreshToken", refreshToken,
            "type", "Bearer",
            "user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "imageUrl", user.getImageUrl() != null ? user.getImageUrl() : picture,
                "roles", user.getRoles()
            )
        );
        
        // Redirect to frontend with token
        String redirectUrl = UriComponentsBuilder.fromUriString(getDefaultTargetUrl())
                .queryParam("token", URLEncoder.encode(token, StandardCharsets.UTF_8))
                .queryParam("refreshToken", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .queryParam("user", URLEncoder.encode(objectMapper.writeValueAsString(responseData.get("user")), StandardCharsets.UTF_8))
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
    
    private User getOrCreateUser(String email, String name, String picture, OAuth2User oAuth2User) {
        Optional<User> existingUser = userRepository.findByEmailAndIsActiveTrue(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info if needed
            boolean updated = false;
            
            if (user.getImageUrl() == null && picture != null) {
                user.setImageUrl(picture);
                updated = true;
            }
            
            if (updated) {
                userRepository.save(user);
            }
            
            return user;
        }
        
        // Create new user
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        User newUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .imageUrl(picture)
                .provider(User.AuthProvider.GOOGLE)
                .providerId(oAuth2User.getName())
                .roles(Set.of(User.Role.USER))
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Created new OAuth2 user: {}", email);
        
        return savedUser;
    }
}
