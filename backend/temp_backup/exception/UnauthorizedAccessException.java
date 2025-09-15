package com.ecommerce.exception;

public class UnauthorizedAccessException extends ValidationException {
    
    public UnauthorizedAccessException(String resourceType, String resourceId) {
        super(String.format("Unauthorized access to %s with ID: %s", resourceType, resourceId));
    }
    
    public UnauthorizedAccessException(String message) {
        super("Unauthorized access: " + message);
    }
}
