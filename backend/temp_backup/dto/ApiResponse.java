package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private Map<String, String> fieldErrors;
    private String timestamp;
    private String path;
    private Integer status;
    
    // Success Response Methods
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .status(201)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(201)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> updated(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Resource updated successfully")
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> deleted() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Resource deleted successfully")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> deleted(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    // Error Response Methods
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, Integer status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, Map<String, String> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors, Map<String, String> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> validationError(String message, Map<String, String> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .fieldErrors(fieldErrors)
                .status(400)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(404)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(401)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(403)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
    
    // Utility Methods
    
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }
    
    public ApiResponse<T> withStatus(Integer status) {
        this.status = status;
        return this;
    }
    
    public ApiResponse<T> withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
