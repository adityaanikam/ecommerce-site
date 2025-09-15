package com.ecommerce.dto;

import com.ecommerce.model.Order;
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
public class OrderDto {
    
    private String id;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Order number is required")
    @Pattern(regexp = "^ORD-[A-Z0-9]{8}$", message = "Order number must be in format ORD-XXXXXXXX")
    private String orderNumber;
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDto> items;
    
    @NotNull(message = "Order status is required")
    private Order.OrderStatus status;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal totalAmount;
    
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
    
    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressDto shippingAddress;
    
    @Valid
    private AddressDto billingAddress;
    
    @Valid
    @NotNull(message = "Payment information is required")
    private PaymentInfoDto paymentInfo;
    
    @Size(max = 500, message = "Order notes cannot exceed 500 characters")
    private String notes;
    
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
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
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        private String lastName;
        
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
        
        @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number should be valid")
        private String phoneNumber;
        
        @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
        private String addressLine2;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfoDto {
        @NotNull(message = "Payment method is required")
        private Order.PaymentMethod paymentMethod;
        
        private String transactionId;
        
        @NotNull(message = "Payment status is required")
        private Order.PaymentStatus paymentStatus;
        
        private LocalDateTime paidAt;
        
        private String paymentGateway;
        private String gatewayTransactionId;
        private BigDecimal refundAmount;
        private LocalDateTime refundedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {
        @Valid
        @NotNull(message = "Shipping address is required")
        private AddressDto shippingAddress;
        
        @Valid
        private AddressDto billingAddress;
        
        @NotNull(message = "Payment method is required")
        private Order.PaymentMethod paymentMethod;
        
        @Size(max = 500, message = "Order notes cannot exceed 500 characters")
        private String notes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private String id;
        private String userId;
        private String orderNumber;
        private List<Order.OrderItem> items;
        private Double totalAmount;
        private Order.OrderStatus status;
        private Order.Address shippingAddress;
        private Order.PaymentMethod paymentMethod;
        private Order.PaymentStatus paymentStatus;
        private String trackingNumber;
        private String carrier;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}