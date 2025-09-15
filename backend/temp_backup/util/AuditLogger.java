package com.ecommerce.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class AuditLogger {
    
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("com.ecommerce.audit");
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("com.ecommerce.security");
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("com.ecommerce.performance");
    
    /**
     * Log user authentication events
     */
    public void logAuthentication(String event, String userId, String email, String ipAddress, boolean success) {
        String message = String.format("AUTH_%s: userId=%s, email=%s, ip=%s, success=%s, timestamp=%s",
                event, userId, email, ipAddress, success, LocalDateTime.now());
        
        if (success) {
            AUDIT_LOGGER.info(message);
        } else {
            SECURITY_LOGGER.warn(message);
        }
    }
    
    /**
     * Log user authorization events
     */
    public void logAuthorization(String event, String userId, String resource, String action, boolean authorized) {
        String message = String.format("AUTHZ_%s: userId=%s, resource=%s, action=%s, authorized=%s, timestamp=%s",
                event, userId, resource, action, authorized, LocalDateTime.now());
        
        if (authorized) {
            AUDIT_LOGGER.info(message);
        } else {
            SECURITY_LOGGER.warn(message);
        }
    }
    
    /**
     * Log data access events
     */
    public void logDataAccess(String event, String userId, String resourceType, String resourceId, String action) {
        String message = String.format("DATA_%s: userId=%s, resourceType=%s, resourceId=%s, action=%s, timestamp=%s",
                event, userId, resourceType, resourceId, action, LocalDateTime.now());
        
        AUDIT_LOGGER.info(message);
    }
    
    /**
     * Log data modification events
     */
    public void logDataModification(String event, String userId, String resourceType, String resourceId, 
                                  String action, Map<String, Object> changes) {
        String message = String.format("MODIFY_%s: userId=%s, resourceType=%s, resourceId=%s, action=%s, changes=%s, timestamp=%s",
                event, userId, resourceType, resourceId, action, changes, LocalDateTime.now());
        
        AUDIT_LOGGER.info(message);
    }
    
    /**
     * Log security events
     */
    public void logSecurityEvent(String event, String userId, String details, String severity) {
        String message = String.format("SECURITY_%s: userId=%s, details=%s, severity=%s, timestamp=%s",
                event, userId, details, severity, LocalDateTime.now());
        
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                SECURITY_LOGGER.error(message);
                break;
            case "HIGH":
                SECURITY_LOGGER.warn(message);
                break;
            case "MEDIUM":
            case "LOW":
            default:
                SECURITY_LOGGER.info(message);
                break;
        }
    }
    
    /**
     * Log performance metrics
     */
    public void logPerformance(String operation, long duration, String userId, Map<String, Object> metrics) {
        String message = String.format("PERF_%s: duration=%dms, userId=%s, metrics=%s, timestamp=%s",
                operation, duration, userId, metrics, LocalDateTime.now());
        
        if (duration > 5000) { // Log slow operations as warnings
            PERFORMANCE_LOGGER.warn(message);
        } else {
            PERFORMANCE_LOGGER.info(message);
        }
    }
    
    /**
     * Log API requests
     */
    public void logApiRequest(String method, String endpoint, String userId, String ipAddress, 
                            int statusCode, long duration) {
        String message = String.format("API_%s: method=%s, endpoint=%s, userId=%s, ip=%s, status=%d, duration=%dms, timestamp=%s",
                method, endpoint, userId, ipAddress, statusCode, duration, LocalDateTime.now());
        
        if (statusCode >= 400) {
            AUDIT_LOGGER.warn(message);
        } else {
            AUDIT_LOGGER.info(message);
        }
    }
    
    /**
     * Log file operations
     */
    public void logFileOperation(String operation, String fileName, String userId, long fileSize, boolean success) {
        String message = String.format("FILE_%s: fileName=%s, userId=%s, size=%d, success=%s, timestamp=%s",
                operation, fileName, userId, fileSize, success, LocalDateTime.now());
        
        if (success) {
            AUDIT_LOGGER.info(message);
        } else {
            AUDIT_LOGGER.warn(message);
        }
    }
    
    /**
     * Log payment events
     */
    public void logPaymentEvent(String event, String userId, String orderId, String amount, String status) {
        String message = String.format("PAYMENT_%s: userId=%s, orderId=%s, amount=%s, status=%s, timestamp=%s",
                event, userId, orderId, amount, status, LocalDateTime.now());
        
        AUDIT_LOGGER.info(message);
    }
    
    /**
     * Log system events
     */
    public void logSystemEvent(String event, String component, String details, String level) {
        String message = String.format("SYSTEM_%s: component=%s, details=%s, level=%s, timestamp=%s",
                event, component, details, level, LocalDateTime.now());
        
        switch (level.toUpperCase()) {
            case "ERROR":
                AUDIT_LOGGER.error(message);
                break;
            case "WARN":
                AUDIT_LOGGER.warn(message);
                break;
            case "INFO":
            default:
                AUDIT_LOGGER.info(message);
                break;
        }
    }
    
    /**
     * Log business events
     */
    public void logBusinessEvent(String event, String userId, String entityType, String entityId, 
                               Map<String, Object> details) {
        String message = String.format("BUSINESS_%s: userId=%s, entityType=%s, entityId=%s, details=%s, timestamp=%s",
                event, userId, entityType, entityId, details, LocalDateTime.now());
        
        AUDIT_LOGGER.info(message);
    }
}
