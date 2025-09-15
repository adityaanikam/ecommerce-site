package com.ecommerce.security;

import com.ecommerce.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class SecurityValidator {
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    // Security validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,50}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile(
        "^\\.(jpg|jpeg|png|gif|webp|pdf|doc|docx|txt)$"
    );
    
    // Rate limiting constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * Validate email format and security
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        
        String sanitizedEmail = inputSanitizer.sanitizeEmail(email);
        
        if (!EMAIL_PATTERN.matcher(sanitizedEmail).matches()) {
            throw new ValidationException("Invalid email format");
        }
        
        if (inputSanitizer.containsMaliciousContent(email)) {
            throw new ValidationException("Email contains potentially malicious content");
        }
        
        // Check for common disposable email domains
        if (isDisposableEmail(sanitizedEmail)) {
            throw new ValidationException("Disposable email addresses are not allowed");
        }
    }
    
    /**
     * Validate phone number format and security
     */
    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Phone number is optional
        }
        
        String sanitizedPhone = inputSanitizer.sanitizePhoneNumber(phoneNumber);
        
        if (!PHONE_PATTERN.matcher(sanitizedPhone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
        
        if (inputSanitizer.containsMaliciousContent(phoneNumber)) {
            throw new ValidationException("Phone number contains potentially malicious content");
        }
    }
    
    /**
     * Validate username format and security
     */
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        
        String sanitizedUsername = inputSanitizer.sanitizeText(username);
        
        if (!USERNAME_PATTERN.matcher(sanitizedUsername).matches()) {
            throw new ValidationException("Username must be 3-50 characters long and contain only letters, numbers, dots, underscores, and hyphens");
        }
        
        if (inputSanitizer.containsMaliciousContent(username)) {
            throw new ValidationException("Username contains potentially malicious content");
        }
        
        // Check for reserved usernames
        if (isReservedUsername(sanitizedUsername)) {
            throw new ValidationException("This username is reserved and cannot be used");
        }
    }
    
    /**
     * Validate password strength and security
     */
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            throw new ValidationException("Password must be no more than 128 characters long");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
        
        if (inputSanitizer.containsMaliciousContent(password)) {
            throw new ValidationException("Password contains potentially malicious content");
        }
        
        // Check for common weak passwords
        if (isWeakPassword(password)) {
            throw new ValidationException("Password is too weak. Please choose a stronger password");
        }
    }
    
    /**
     * Validate file upload security
     */
    public void validateFileUpload(String fileName, long fileSize, String contentType) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ValidationException("File name is required");
        }
        
        String sanitizedFileName = inputSanitizer.sanitizeFileName(fileName);
        
        if (inputSanitizer.containsMaliciousContent(fileName)) {
            throw new ValidationException("File name contains potentially malicious content");
        }
        
        // Check file size
        if (fileSize > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed size of 10MB");
        }
        
        // Check file extension
        String extension = getFileExtension(sanitizedFileName);
        if (extension == null || !FILE_EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new ValidationException("File type not allowed. Allowed types: jpg, jpeg, png, gif, webp, pdf, doc, docx, txt");
        }
        
        // Check content type
        if (contentType != null && !isAllowedContentType(contentType)) {
            throw new ValidationException("Content type not allowed");
        }
    }
    
    /**
     * Validate input length limits
     */
    public void validateInputLength(String input, String fieldName, int maxLength) {
        if (input != null && input.length() > maxLength) {
            throw new ValidationException(fieldName + " must be no more than " + maxLength + " characters long");
        }
    }
    
    /**
     * Validate numeric input ranges
     */
    public void validateNumericRange(Number value, String fieldName, Number min, Number max) {
        if (value == null) {
            return;
        }
        
        double doubleValue = value.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        
        if (doubleValue < minValue || doubleValue > maxValue) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
    }
    
    /**
     * Validate URL security
     */
    public void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        
        if (inputSanitizer.containsMaliciousContent(url)) {
            throw new ValidationException("URL contains potentially malicious content");
        }
        
        // Check for allowed protocols
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ValidationException("URL must start with http:// or https://");
        }
    }
    
    /**
     * Check if email is from a disposable email service
     */
    private boolean isDisposableEmail(String email) {
        String[] disposableDomains = {
            "10minutemail.com", "tempmail.org", "guerrillamail.com", 
            "mailinator.com", "throwaway.email", "temp-mail.org"
        };
        
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        
        for (String disposableDomain : disposableDomains) {
            if (domain.equals(disposableDomain)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if username is reserved
     */
    private boolean isReservedUsername(String username) {
        String[] reservedUsernames = {
            "admin", "administrator", "root", "user", "guest", "test", 
            "api", "www", "mail", "support", "help", "info", "contact"
        };
        
        String lowerUsername = username.toLowerCase();
        
        for (String reserved : reservedUsernames) {
            if (lowerUsername.equals(reserved)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if password is weak
     */
    private boolean isWeakPassword(String password) {
        String[] weakPasswords = {
            "password", "123456", "123456789", "qwerty", "abc123", 
            "password123", "admin", "letmein", "welcome", "monkey"
        };
        
        String lowerPassword = password.toLowerCase();
        
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        }
        return fileName.substring(lastDotIndex);
    }
    
    /**
     * Check if content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowedTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        };
        
        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        
        return false;
    }
}
