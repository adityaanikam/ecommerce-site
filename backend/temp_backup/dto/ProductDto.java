package com.ecommerce.dto;

import com.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private String id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String name;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "Discount price must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Discount price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal discountPrice;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String subcategory;
    
    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    private String brand;
    
    @NotEmpty(message = "At least one image is required")
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> images;
    
    private Map<String, Object> specifications;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stock;
    
    private Boolean isActive;
    
    @NotBlank(message = "Seller ID is required")
    private String sellerId;
    
    private Double averageRating;
    private Integer totalReviews;
    
    @Valid
    private List<ReviewDto> reviews;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
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
        
        private LocalDateTime createdAt;
        private Boolean isVerified;
        private List<String> images;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificationDto {
        @NotBlank(message = "Specification name is required")
        private String name;
        
        @NotBlank(message = "Specification value is required")
        private String value;
        
        private String unit;
        private String type;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
        private String name;
        
        @NotBlank(message = "Product description is required")
        @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters")
        private String description;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private Double price;
        
        @DecimalMin(value = "0.00", message = "Discount price must be greater than or equal to 0")
        private Double discountPrice;
        
        @NotBlank(message = "Category ID is required")
        private String categoryId;
        
        private String subcategoryId;
        
        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand name cannot exceed 100 characters")
        private String brand;
        
        private List<String> images;
        
        private Map<String, Object> specifications;
        
        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stock;
        
        @NotBlank(message = "Seller ID is required")
        private String sellerId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProductRequest {
        @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
        private String name;
        
        @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters")
        private String description;
        
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private Double price;
        
        @DecimalMin(value = "0.00", message = "Discount price must be greater than or equal to 0")
        private Double discountPrice;
        
        private String categoryId;
        
        private String subcategoryId;
        
        @Size(max = 100, message = "Brand name cannot exceed 100 characters")
        private String brand;
        
        private List<String> images;
        
        private Map<String, Object> specifications;
        
        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stock;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResponse {
        private String id;
        private String name;
        private String description;
        private Double price;
        private Double discountPrice;
        private String category;
        private String subcategory;
        private String brand;
        private List<String> images;
        private Map<String, Object> specifications;
        private Integer stock;
        private Product.Ratings ratings;
        private List<Product.Review> reviews;
        private String sellerId;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchRequest {
        private String query;
        private String categoryId;
        private String subcategoryId;
        private String brand;
        private Double minPrice;
        private Double maxPrice;
        private Double minRating;
        private String sortBy; // price, rating, name, createdAt
        private String sortDirection; // asc, desc
        private Boolean inStock;
        private List<String> tags;
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
}