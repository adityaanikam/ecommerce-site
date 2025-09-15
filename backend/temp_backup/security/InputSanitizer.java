package com.ecommerce.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class InputSanitizer {
    
    // XSS Prevention Patterns
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONERROR_PATTERN = Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("onclick\\s*=", Pattern.CASE_INSENSITIVE);
    
    // SQL Injection Prevention Patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(union|select|insert|update|delete|drop|create|alter|exec|execute|script)\\s+", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Path Traversal Prevention
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(\\.\\./|\\.\\.\\\\)");
    
    // HTML Tags Pattern
    private static final Pattern HTML_TAGS_PATTERN = Pattern.compile("<[^>]*>");
    
    /**
     * Sanitize input string to prevent XSS attacks
     */
    public String sanitizeForXSS(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = input;
        
        // Remove script tags
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove javascript: protocols
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove event handlers
        sanitized = ONLOAD_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ONERROR_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ONCLICK_PATTERN.matcher(sanitized).replaceAll("");
        
        // HTML encode special characters
        sanitized = htmlEncode(sanitized);
        
        log.debug("XSS sanitization applied to input: {} -> {}", input, sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize input string to prevent SQL injection
     */
    public String sanitizeForSQLInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = input;
        
        // Remove SQL injection patterns
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        
        // Escape single quotes
        sanitized = sanitized.replace("'", "''");
        
        log.debug("SQL injection sanitization applied to input: {} -> {}", input, sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize file path to prevent path traversal attacks
     */
    public String sanitizeFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return filePath;
        }
        
        String sanitized = filePath;
        
        // Remove path traversal patterns
        sanitized = PATH_TRAVERSAL_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove any remaining special characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._/-]", "");
        
        log.debug("Path traversal sanitization applied to input: {} -> {}", filePath, sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Remove HTML tags from input
     */
    public String removeHtmlTags(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = HTML_TAGS_PATTERN.matcher(input).replaceAll("");
        
        log.debug("HTML tags removed from input: {} -> {}", input, sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize email input
     */
    public String sanitizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return email;
        }
        
        String sanitized = email.toLowerCase().trim();
        
        // Remove any HTML tags
        sanitized = removeHtmlTags(sanitized);
        
        // Remove any script content
        sanitized = sanitizeForXSS(sanitized);
        
        log.debug("Email sanitization applied: {} -> {}", email, sanitized);
        
        return sanitized;
    }
    
    /**
     * Sanitize phone number input
     */
    public String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        
        // Remove all non-digit characters except + at the beginning
        String sanitized = phoneNumber.replaceAll("[^\\d+]", "");
        
        // Ensure + is only at the beginning
        if (sanitized.startsWith("+")) {
            sanitized = "+" + sanitized.substring(1).replaceAll("[^\\d]", "");
        } else {
            sanitized = sanitized.replaceAll("[^\\d]", "");
        }
        
        log.debug("Phone number sanitization applied: {} -> {}", phoneNumber, sanitized);
        
        return sanitized;
    }
    
    /**
     * Sanitize general text input
     */
    public String sanitizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        String sanitized = text;
        
        // Remove HTML tags
        sanitized = removeHtmlTags(sanitized);
        
        // Sanitize for XSS
        sanitized = sanitizeForXSS(sanitized);
        
        // Remove excessive whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        log.debug("Text sanitization applied: {} -> {}", text, sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * HTML encode special characters
     */
    private String htmlEncode(String input) {
        if (input == null) {
            return null;
        }
        
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }
    
    /**
     * Validate and sanitize file name
     */
    public String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return fileName;
        }
        
        String sanitized = fileName;
        
        // Remove path traversal
        sanitized = sanitizeFilePath(sanitized);
        
        // Remove special characters except dots and hyphens
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");
        
        // Remove multiple dots
        sanitized = sanitized.replaceAll("\\.{2,}", ".");
        
        // Limit length
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        log.debug("File name sanitization applied: {} -> {}", fileName, sanitized);
        
        return sanitized;
    }
    
    /**
     * Check if input contains potentially malicious content
     */
    public boolean containsMaliciousContent(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        return SCRIPT_PATTERN.matcher(input).find() ||
               JAVASCRIPT_PATTERN.matcher(input).find() ||
               SQL_INJECTION_PATTERN.matcher(input).find() ||
               PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }
}
