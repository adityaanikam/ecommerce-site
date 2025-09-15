package com.ecommerce.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    
    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}
