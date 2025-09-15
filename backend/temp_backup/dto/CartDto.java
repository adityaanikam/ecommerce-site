package com.ecommerce.dto;

import com.ecommerce.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    
    private String id;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @Valid
    private List<CartItemDto> items;
    
    @DecimalMin(value = "0.00", message = "Total amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal totalAmount;
    
    @Min(value = 0, message = "Total items cannot be negative")
    private Integer totalItems;
    
    @DecimalMin(value = "0.00", message = "Subtotal must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Subtotal must have at most 10 integer digits and 2 decimal places")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.00", message = "Tax amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.00", message = "Shipping amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Shipping amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal shippingAmount;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal discountAmount;
    
    private String couponCode;
    private BigDecimal couponDiscount;
    
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
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
        
        private Boolean isAvailable;
        private Integer maxQuantity;
        private LocalDateTime addedAt;
        
        // Product variant information
        private String variant;
        private String size;
        private String color;
        
        // Seller information
        private String sellerId;
        private String sellerName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartResponse {
        private String id;
        private String userId;
        private List<Cart.CartItem> items;
        private Double totalAmount;
        private Double taxAmount;
        private Double shippingAmount;
        private Double discountAmount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartSummaryResponse {
        private Integer itemCount;
        private Integer totalItems;
        private Double subtotal;
        private Double taxAmount;
        private Double shippingAmount;
        private Double discountAmount;
        private Double totalAmount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemCountResponse {
        private Integer itemCount;
        private Boolean isEmpty;
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