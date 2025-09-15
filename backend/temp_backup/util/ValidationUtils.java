package com.ecommerce.util;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ValidationUtils {
    
    // Common validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,50}$"
    );
    
    /**
     * Convert BindingResult to field errors map
     */
    public Map<String, String> getFieldErrors(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        return fieldErrors;
    }
    
    /**
     * Create validation error response from BindingResult
     */
    public ApiResponse<Object> createValidationErrorResponse(BindingResult bindingResult, String message) {
        Map<String, String> fieldErrors = getFieldErrors(bindingResult);
        return ApiResponse.validationError(message, fieldErrors);
    }
    
    /**
     * Validate email format
     */
    public void validateEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid " + fieldName + " format");
        }
    }
    
    /**
     * Validate phone number format
     */
    public void validatePhoneNumber(String phoneNumber, String fieldName) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Phone number is optional
        }
        
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new ValidationException("Invalid " + fieldName + " format");
        }
    }
    
    /**
     * Validate password strength
     */
    public void validatePassword(String password, String fieldName) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (password.length() < 8) {
            throw new ValidationException(fieldName + " must be at least 8 characters long");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException(fieldName + " must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }
    
    /**
     * Validate username format
     */
    public void validateUsername(String username, String fieldName) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(fieldName + " must be 3-50 characters long and contain only letters, numbers, dots, underscores, and hyphens");
        }
    }
    
    /**
     * Validate string length
     */
    public void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (value.length() < minLength) {
            throw new ValidationException(fieldName + " must be at least " + minLength + " characters long");
        }
        
        if (value.length() > maxLength) {
            throw new ValidationException(fieldName + " must be no more than " + maxLength + " characters long");
        }
    }
    
    /**
     * Validate numeric range
     */
    public void validateNumericRange(Number value, String fieldName, Number min, Number max) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        double doubleValue = value.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        
        if (doubleValue < minValue || doubleValue > maxValue) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
    }
    
    /**
     * Validate positive number
     */
    public void validatePositiveNumber(Number value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }
    
    /**
     * Validate non-negative number
     */
    public void validateNonNegativeNumber(Number value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }
    
    /**
     * Validate required field
     */
    public void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
    
    /**
     * Validate enum value
     */
    public <T extends Enum<T>> void validateEnum(String value, String fieldName, Class<T> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        try {
            Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid " + fieldName + " value: " + value);
        }
    }
    
    /**
     * Validate URL format
     */
    public void validateUrl(String url, String fieldName) {
        if (url == null || url.trim().isEmpty()) {
            return; // URL is optional
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ValidationException(fieldName + " must start with http:// or https://");
        }
        
        // Basic URL format validation
        try {
            new java.net.URL(url);
        } catch (Exception e) {
            throw new ValidationException("Invalid " + fieldName + " format");
        }
    }
    
    /**
     * Validate file extension
     */
    public void validateFileExtension(String fileName, String fieldName, String[] allowedExtensions) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        String extension = getFileExtension(fileName);
        if (extension == null) {
            throw new ValidationException(fieldName + " must have a file extension");
        }
        
        boolean isValid = false;
        for (String allowedExt : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowedExt)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new ValidationException(fieldName + " must have one of the following extensions: " + 
                    String.join(", ", allowedExtensions));
        }
    }
    
    /**
     * Validate file size
     */
    public void validateFileSize(long fileSize, String fieldName, long maxSize) {
        if (fileSize > maxSize) {
            throw new ValidationException(fieldName + " size exceeds maximum allowed size of " + 
                    formatFileSize(maxSize));
        }
    }
    
    /**
     * Validate date range
     */
    public void validateDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, 
                                String fieldName) {
        if (startDate == null || endDate == null) {
            throw new ValidationException(fieldName + " dates are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException(fieldName + " start date must be before end date");
        }
    }
    
    /**
     * Validate collection size
     */
    public void validateCollectionSize(java.util.Collection<?> collection, String fieldName, 
                                     int minSize, int maxSize) {
        if (collection == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (collection.size() < minSize) {
            throw new ValidationException(fieldName + " must contain at least " + minSize + " items");
        }
        
        if (collection.size() > maxSize) {
            throw new ValidationException(fieldName + " must contain no more than " + maxSize + " items");
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        }
        return fileName.substring(lastDotIndex + 1);
    }
    
    /**
     * Format file size in human readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Create success response with validation message
     */
    public ApiResponse<Object> createSuccessResponse(String message) {
        return ApiResponse.success(message);
    }
    
    /**
     * Create success response with data
     */
    public <T> ApiResponse<T> createSuccessResponse(T data, String message) {
        return ApiResponse.success(data, message);
    }
    
    /**
     * Create error response
     */
    public ApiResponse<Object> createErrorResponse(String message) {
        return ApiResponse.error(message);
    }
    
    /**
     * Create validation error response
     */
    public ApiResponse<Object> createValidationErrorResponse(String message, Map<String, String> fieldErrors) {
        return ApiResponse.validationError(message, fieldErrors);
    }
}
