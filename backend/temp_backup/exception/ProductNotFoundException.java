package com.ecommerce.exception;

public class ProductNotFoundException extends ResourceNotFoundException {
    
    public ProductNotFoundException(String productId) {
        super("Product not found with ID: " + productId);
    }
    
    public ProductNotFoundException(String field, String value) {
        super("Product not found with " + field + ": " + value);
    }
}
