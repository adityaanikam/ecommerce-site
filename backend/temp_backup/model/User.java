package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
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
    
    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);
    
    private List<Address> addresses;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @Field("is_active")
    private Boolean isActive = true;
    
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;
    
    private String providerId;
    
    private String imageUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
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
        
        @Builder.Default
        private Boolean isDefault = false;
        
        @Size(max = 50, message = "Address type cannot exceed 50 characters")
        private String addressType; // HOME, WORK, BILLING, SHIPPING
    }
    
    public enum Role {
        USER, ADMIN, MODERATOR, SELLER
    }
    
    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK, GITHUB
    }
    
    // Additional methods for OAuth2 integration
    public String getImageUrl() {
        return this.imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getProviderId() {
        return this.providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}