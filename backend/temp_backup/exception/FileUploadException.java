package com.ecommerce.exception;

public class FileUploadException extends ValidationException {
    
    public FileUploadException(String message) {
        super("File upload failed: " + message);
    }
    
    public FileUploadException(String fileName, String reason) {
        super(String.format("File upload failed for %s: %s", fileName, reason));
    }
}
