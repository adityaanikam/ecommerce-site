package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {
    
    @Id
    private String id;
    
    @NotBlank(message = "Product ID is required")
    @Indexed
    @Field("product_id")
    private String productId;
    
    @NotBlank(message = "User ID is required")
    @Indexed
    @Field("user_id")
    private String userId;
    
    @NotBlank(message = "User name is required")
    @Size(max = 100, message = "User name cannot exceed 100 characters")
    @Field("user_name")
    private String userName;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Review comment cannot exceed 1000 characters")
    private String comment;
    
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> images;
    
    @Builder.Default
    @Field("is_verified")
    private Boolean isVerified = false;
    
    @Builder.Default
    @Field("is_helpful")
    private Integer helpfulCount = 0;
    
    @Builder.Default
    @Field("is_not_helpful")
    private Integer notHelpfulCount = 0;
    
    @Builder.Default
    @Field("is_approved")
    private Boolean isApproved = true;
    
    @Field("order_id")
    private String orderId;
    
    @Field("purchase_date")
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
    @Field("seller_response")
    private String sellerResponse;
    
    @Field("seller_response_date")
    private LocalDateTime sellerResponseDate;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("reported_count")
    @Builder.Default
    private Integer reportedCount = 0;
    
    @Field("is_flagged")
    @Builder.Default
    private Boolean isFlagged = false;
    
    // Review moderation
    @Field("moderation_status")
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;
    
    @Field("moderator_notes")
    private String moderatorNotes;
    
    @Field("moderated_at")
    private LocalDateTime moderatedAt;
    
    @Field("moderated_by")
    private String moderatedBy;
    
    public enum ModerationStatus {
        PENDING, APPROVED, REJECTED, FLAGGED
    }
}
