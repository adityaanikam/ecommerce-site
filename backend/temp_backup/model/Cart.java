package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {
    
    @Id
    private String id;
    
    @NotBlank(message = "User ID is required")
    @Indexed(unique = true)
    private String userId;
    
    @Valid
    private List<CartItem> items;
    
    @DecimalMin(value = "0.00", message = "Total amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most 10 integer digits and 2 decimal places")
    @Field("total_amount")
    private BigDecimal totalAmount;
    
    @Min(value = 0, message = "Total items cannot be negative")
    @Field("total_items")
    private Integer totalItems;
    
    @DecimalMin(value = "0.00", message = "Subtotal must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Subtotal must have at most 10 integer digits and 2 decimal places")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.00", message = "Tax amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    @Field("tax_amount")
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.00", message = "Shipping amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Shipping amount must have at most 10 integer digits and 2 decimal places")
    @Field("shipping_amount")
    private BigDecimal shippingAmount;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must have at most 10 integer digits and 2 decimal places")
    @Field("discount_amount")
    private BigDecimal discountAmount;
    
    @Field("coupon_code")
    private String couponCode;
    
    @Field("coupon_discount")
    private BigDecimal couponDiscount;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("expires_at")
    private LocalDateTime expiresAt;
    
    @Builder.Default
    @Field("is_active")
    private Boolean isActive = true;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        @NotBlank(message = "Product ID is required")
        @Field("product_id")
        private String productId;
        
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        @Field("product_name")
        private String productName;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        private BigDecimal price;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;
        
        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        @Field("image_url")
        private String imageUrl;
        
        @NotNull(message = "Subtotal is required")
        @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Subtotal must have at most 10 integer digits and 2 decimal places")
        private BigDecimal subtotal;
        
        @Size(max = 100, message = "SKU cannot exceed 100 characters")
        private String sku;
        
        @Size(max = 100, message = "Brand cannot exceed 100 characters")
        private String brand;
        
        @Size(max = 100, message = "Category cannot exceed 100 characters")
        private String category;
        
        @Field("is_available")
        private Boolean isAvailable;
        
        @Field("max_quantity")
        private Integer maxQuantity;
        
        @Field("added_at")
        private LocalDateTime addedAt;
        
        // Product variant information
        private String variant;
        private String size;
        private String color;
        
        // Seller information
        @Field("seller_id")
        private String sellerId;
        
        @Field("seller_name")
        private String sellerName;
    }
}