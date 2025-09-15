package com.ecommerce.dto;

import com.ecommerce.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    
    private String id;
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
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
    
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> images;
    
    private Boolean isVerified;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Boolean isApproved;
    
    private String orderId;
    private LocalDateTime purchaseDate;
    
    // Review metadata
    @Size(max = 50, message = "Review title cannot exceed 50 characters")
    private String title;
    
    private List<String> pros;
    private List<String> cons;
    
    // Product variant information
    private String variant;
    private String size;
    private String color;
    
    // Seller response
    private String sellerResponse;
    private LocalDateTime sellerResponseDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Integer reportedCount;
    private Boolean isFlagged;
    
    // Review moderation
    private Review.ModerationStatus moderationStatus;
    private String moderatorNotes;
    private LocalDateTime moderatedAt;
    private String moderatedBy;
    
    // Inner classes for request/response DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        @Size(max = 1000, message = "Review comment cannot exceed 1000 characters")
        private String comment;
        
        @Size(max = 10, message = "Maximum 10 images allowed")
        private List<String> images;
        
        @Size(max = 50, message = "Review title cannot exceed 50 characters")
        private String title;
        
        private List<String> pros;
        private List<String> cons;
        
        private String variant;
        private String size;
        private String color;
        
        private String orderId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateReviewRequest {
        @Size(max = 1000, message = "Review comment cannot exceed 1000 characters")
        private String comment;
        
        @Size(max = 10, message = "Maximum 10 images allowed")
        private List<String> images;
        
        @Size(max = 50, message = "Review title cannot exceed 50 characters")
        private String title;
        
        private List<String> pros;
        private List<String> cons;
        
        private String variant;
        private String size;
        private String color;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponse {
        private String id;
        private String productId;
        private String userId;
        private String userName;
        private Integer rating;
        private String comment;
        private List<String> images;
        private Boolean isVerified;
        private Integer helpfulCount;
        private Integer notHelpfulCount;
        private Boolean isApproved;
        private String orderId;
        private LocalDateTime purchaseDate;
        private String title;
        private List<String> pros;
        private List<String> cons;
        private String variant;
        private String size;
        private String color;
        private String sellerResponse;
        private LocalDateTime sellerResponseDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer reportedCount;
        private Boolean isFlagged;
        private Review.ModerationStatus moderationStatus;
        private String moderatorNotes;
        private LocalDateTime moderatedAt;
        private String moderatedBy;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRatingResponse {
        private String productId;
        private Double averageRating;
        private Integer totalReviews;
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
