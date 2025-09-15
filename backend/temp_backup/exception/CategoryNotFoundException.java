package com.ecommerce.exception;

public class CategoryNotFoundException extends ResourceNotFoundException {
    
    public CategoryNotFoundException(String categoryId) {
        super("Category not found with ID: " + categoryId);
    }
    
    public CategoryNotFoundException(String field, String value) {
        super("Category not found with " + field + ": " + value);
    }
}
