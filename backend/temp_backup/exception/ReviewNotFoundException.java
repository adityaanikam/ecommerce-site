package com.ecommerce.exception;

public class ReviewNotFoundException extends ResourceNotFoundException {
    
    public ReviewNotFoundException(String reviewId) {
        super("Review not found with ID: " + reviewId);
    }
    
    public ReviewNotFoundException(String field, String value) {
        super("Review not found with " + field + ": " + value);
    }
}
