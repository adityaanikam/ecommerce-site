package com.ecommerce.dto;

import com.ecommerce.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Data
public class AuthDto {
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", 
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        private String password;
        
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name can only contain letters and spaces")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name can only contain letters and spaces")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number should be valid")
        private String phone;
    }
    
    @Data
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private String type = "Bearer";
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String imageUrl;
        private Set<User.Role> roles;
        private User.AuthProvider provider;
    }
    
    @Data
    public static class PasswordChangeRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;
        
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", 
                message = "New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        private String newPassword;
        
        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }
    
    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
    }
    
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;
        
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", 
                message = "New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        private String newPassword;
        
        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }
    
    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
    
    @Data
    public static class EmailVerificationRequest {
        @NotBlank(message = "Token is required")
        private String token;
    }
    
    @Data
    public static class MessageResponse {
        private String message;
        private String timestamp;
        
        public MessageResponse(String message) {
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
    }
}