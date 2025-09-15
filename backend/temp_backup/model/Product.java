package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @TextIndexed
    private String name;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters")
    @TextIndexed
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "Discount price must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Discount price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal discountPrice;
    
    @NotBlank(message = "Category is required")
    @Indexed
    private String category;
    
    @Indexed
    private String subcategory;
    
    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    @Indexed
    private String brand;
    
    @NotEmpty(message = "At least one image is required")
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> images;
    
    private Map<String, Object> specifications;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stock;
    
    @Builder.Default
    @Field("is_active")
    private Boolean isActive = true;
    
    @NotBlank(message = "Seller ID is required")
    @Indexed
    private String sellerId;
    
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Builder.Default
    private Integer totalReviews = 0;
    
    private List<Review> reviews;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        @NotBlank(message = "User ID is required")
        private String userId;
        
        @NotBlank(message = "User name is required")
        @Size(max = 100, message = "User name cannot exceed 100 characters")
        private String userName;
        
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        @Size(max = 1000, message = "Review comment cannot exceed 1000 characters")
        private String comment;
        
        @CreatedDate
        @Field("created_at")
        private LocalDateTime createdAt;
        
        @Builder.Default
        private Boolean isVerified = false;
        
        private List<String> images;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Specification {
        @NotBlank(message = "Specification name is required")
        private String name;
        
        @NotBlank(message = "Specification value is required")
        private String value;
        
        private String unit;
        private String type; // TEXT, NUMBER, BOOLEAN, LIST
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ratings {
        private Double averageRating;
        private Integer totalRatings;
        private Integer rating1Count;
        private Integer rating2Count;
        private Integer rating3Count;
        private Integer rating4Count;
        private Integer rating5Count;
        private Integer verifiedReviewsCount;
        private Integer reviewsWithImagesCount;
        private Integer reviewsWithCommentsCount;
    }
}