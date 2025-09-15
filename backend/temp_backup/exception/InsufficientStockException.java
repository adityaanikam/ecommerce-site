package com.ecommerce.exception;

public class InsufficientStockException extends ValidationException {
    
    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d", 
                productId, requested, available));
    }
    
    public InsufficientStockException(String message) {
        super(message);
    }
}
