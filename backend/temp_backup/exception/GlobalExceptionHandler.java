package com.ecommerce.exception;

import com.ecommerce.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Custom Exceptions
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    // Specific Business Exception Handlers
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request) {
        log.error("Product not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        log.error("User not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCategoryNotFoundException(CategoryNotFoundException ex, HttpServletRequest request) {
        log.error("Category not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        log.error("Order not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCartNotFoundException(CartNotFoundException ex, HttpServletRequest request) {
        log.error("Cart not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleReviewNotFoundException(ReviewNotFoundException ex, HttpServletRequest request) {
        log.error("Review not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.notFound(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientStockException(InsufficientStockException ex, HttpServletRequest request) {
        log.error("Insufficient stock: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleOrderProcessingException(OrderProcessingException ex, HttpServletRequest request) {
        log.error("Order processing error: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        log.error("Duplicate resource: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedAccessException(UnauthorizedAccessException ex, HttpServletRequest request) {
        log.error("Unauthorized access: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.forbidden(ex.getMessage())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex, HttpServletRequest request) {
        log.error("File upload error: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    // Spring Validation Exceptions
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = ApiResponse.validationError("Request validation failed", fieldErrors)
                .withPath(request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error("Missing request parameter: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("Type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(message)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    // Security Exceptions
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Invalid credentials or authentication required")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Bad Credentials")
                .message("Invalid email or password")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You don't have permission to access this resource")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    // HTTP Exceptions
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.error("Method not supported: {}", ex.getMessage());
        
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods: %s", 
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(message)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("No handler found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested endpoint was not found")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    // File Upload Exceptions
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.error("File upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("File Too Large")
                .message("The uploaded file exceeds the maximum allowed size")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    // JSON Parsing Exceptions
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("Invalid JSON: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid JSON")
                .message("The request body contains invalid JSON")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    // Generic Exception Handler
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    // Error Response Classes
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> fieldErrors;
    }
}