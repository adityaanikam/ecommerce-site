package com.ecommerce.exception;

public class CartNotFoundException extends ResourceNotFoundException {
    
    public CartNotFoundException(String userId) {
        super("Cart not found for user ID: " + userId);
    }
    
    public CartNotFoundException(String field, String value) {
        super("Cart not found with " + field + ": " + value);
    }
}
