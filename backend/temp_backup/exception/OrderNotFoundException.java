package com.ecommerce.exception;

public class OrderNotFoundException extends ResourceNotFoundException {
    
    public OrderNotFoundException(String orderId) {
        super("Order not found with ID: " + orderId);
    }
    
    public OrderNotFoundException(String field, String value) {
        super("Order not found with " + field + ": " + value);
    }
}
