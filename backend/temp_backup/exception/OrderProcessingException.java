package com.ecommerce.exception;

public class OrderProcessingException extends ValidationException {
    
    public OrderProcessingException(String message) {
        super("Order processing failed: " + message);
    }
    
    public OrderProcessingException(String orderId, String reason) {
        super(String.format("Order processing failed for order %s: %s", orderId, reason));
    }
}
