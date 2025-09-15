package com.ecommerce.exception;

public class DuplicateResourceException extends ValidationException {
    
    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceType, field, value));
    }
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}
