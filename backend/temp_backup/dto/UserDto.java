package com.ecommerce.dto;

import com.ecommerce.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private String id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
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
    
    private Set<User.Role> roles;
    
    @Valid
    private List<AddressDto> addresses;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Boolean isActive;
    private User.AuthProvider provider;
    private String providerId;
    private String imageUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        @NotBlank(message = "Street address is required")
        @Size(max = 200, message = "Street address cannot exceed 200 characters")
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City name cannot exceed 100 characters")
        private String city;
        
        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State name cannot exceed 100 characters")
        private String state;
        
        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Zip code should be in format 12345 or 12345-6789")
        private String zipCode;
        
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country name cannot exceed 100 characters")
        private String country;
        
        private Boolean isDefault;
        
        @Size(max = 50, message = "Address type cannot exceed 50 characters")
        private String addressType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number should be valid")
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number should be valid")
        private String phone;
        
        private String imageUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String imageUrl;
        private Set<User.Role> roles;
        private List<User.Address> addresses;
        private User.AuthProvider provider;
        private String providerId;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotBlank(message = "Street address is required")
        @Size(max = 200, message = "Street address cannot exceed 200 characters")
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City name cannot exceed 100 characters")
        private String city;
        
        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State name cannot exceed 100 characters")
        private String state;
        
        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Zip code should be in format 12345 or 12345-6789")
        private String zipCode;
        
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country name cannot exceed 100 characters")
        private String country;
        
        private Boolean isDefault;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private String timestamp;
        
        public MessageResponse(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now().toString();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderHistoryResponse {
        private String orderId;
        private String orderNumber;
        private String status;
        private Double totalAmount;
        private LocalDateTime createdAt;
        private List<String> productNames;
    }
}