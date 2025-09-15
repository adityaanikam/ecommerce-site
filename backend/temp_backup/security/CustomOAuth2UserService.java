package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + ex.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        Optional<User> userOptional = userRepository.findByEmailAndIsActiveTrue(email);
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update user info if needed
            updateUserInfo(user, name, picture, registrationId);
        } else {
            // Create new user
            user = createNewUser(email, name, picture, registrationId, oAuth2User.getName());
        }
        
        return new CustomOAuth2User(user, attributes);
    }
    
    private void updateUserInfo(User user, String name, String picture, String registrationId) {
        boolean updated = false;
        
        if (user.getImageUrl() == null && picture != null) {
            user.setImageUrl(picture);
            updated = true;
        }
        
        if (user.getProvider() == null) {
            user.setProvider(User.AuthProvider.valueOf(registrationId.toUpperCase()));
            updated = true;
        }
        
        if (updated) {
            userRepository.save(user);
            log.info("Updated OAuth2 user info for: {}", user.getEmail());
        }
    }
    
    private User createNewUser(String email, String name, String picture, String registrationId, String providerId) {
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        User newUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .imageUrl(picture)
                .provider(User.AuthProvider.valueOf(registrationId.toUpperCase()))
                .providerId(providerId)
                .roles(Set.of(User.Role.USER))
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Created new OAuth2 user: {}", email);
        
        return savedUser;
    }
}
