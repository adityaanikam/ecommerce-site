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

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @NotBlank(message = "User ID is required")
    @Indexed
    private String userId;
    
    @NotBlank(message = "Order number is required")
    @Indexed(unique = true)
    @Pattern(regexp = "^ORD-[A-Z0-9]{8}$", message = "Order number must be in format ORD-XXXXXXXX")
    @Field("order_number")
    private String orderNumber;
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItem> items;
    
    @NotNull(message = "Order status is required")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most 10 integer digits and 2 decimal places")
    @Field("total_amount")
    private BigDecimal totalAmount;
    
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
    
    @Valid
    @NotNull(message = "Shipping address is required")
    @Field("shipping_address")
    private Address shippingAddress;
    
    @Valid
    @Field("billing_address")
    private Address billingAddress;
    
    @Valid
    @NotNull(message = "Payment information is required")
    @Field("payment_info")
    private PaymentInfo paymentInfo;
    
    @Size(max = 500, message = "Order notes cannot exceed 500 characters")
    private String notes;
    
    @Field("tracking_number")
    private String trackingNumber;
    
    @Field("estimated_delivery")
    private LocalDateTime estimatedDelivery;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("shipped_at")
    private LocalDateTime shippedAt;
    
    @Field("delivered_at")
    private LocalDateTime deliveredAt;
    
    @Field("cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
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
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        @Field("first_name")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        @Field("last_name")
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
        @Field("zip_code")
        private String zipCode;
        
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country name cannot exceed 100 characters")
        private String country;
        
        @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number should be valid")
        @Field("phone_number")
        private String phoneNumber;
        
        @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
        @Field("address_line_2")
        private String addressLine2;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        @NotNull(message = "Payment method is required")
        @Field("payment_method")
        private PaymentMethod paymentMethod;
        
        @Field("transaction_id")
        private String transactionId;
        
        @NotNull(message = "Payment status is required")
        @Builder.Default
        @Field("payment_status")
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;
        
        @Field("paid_at")
        private LocalDateTime paidAt;
        
        @Field("payment_gateway")
        private String paymentGateway;
        
        @Field("gateway_transaction_id")
        private String gatewayTransactionId;
        
        @Field("refund_amount")
        private BigDecimal refundAmount;
        
        @Field("refunded_at")
        private LocalDateTime refundedAt;
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED, RETURNED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, STRIPE, BANK_TRANSFER, CASH_ON_DELIVERY, WALLET
    }
    
    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }
}