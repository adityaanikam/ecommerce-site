package com.ecommerce.util;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ErrorResponseBuilder {
    
    /**
     * Build validation error response from BindingResult
     */
    public ApiResponse<Object> buildValidationErrorResponse(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new HashMap<>();
        List<String> globalErrors = bindingResult.getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());
        
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        String message = globalErrors.isEmpty() ? "Validation failed" : String.join(", ", globalErrors);
        
        return ApiResponse.validationError(message, fieldErrors);
    }
    
    /**
     * Build validation error response from field errors map
     */
    public ApiResponse<Object> buildValidationErrorResponse(Map<String, String> fieldErrors) {
        return ApiResponse.validationError("Validation failed", fieldErrors);
    }
    
    /**
     * Build validation error response from ValidationException
     */
    public ApiResponse<Object> buildValidationErrorResponse(ValidationException ex) {
        return ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }
    
    /**
     * Build not found error response
     */
    public ApiResponse<Object> buildNotFoundErrorResponse(String resource, String identifier) {
        String message = String.format("%s not found with identifier: %s", resource, identifier);
        return ApiResponse.notFound(message);
    }
    
    /**
     * Build unauthorized error response
     */
    public ApiResponse<Object> buildUnauthorizedErrorResponse(String message) {
        return ApiResponse.unauthorized(message);
    }
    
    /**
     * Build forbidden error response
     */
    public ApiResponse<Object> buildForbiddenErrorResponse(String message) {
        return ApiResponse.forbidden(message);
    }
    
    /**
     * Build conflict error response
     */
    public ApiResponse<Object> buildConflictErrorResponse(String message) {
        return ApiResponse.error(message, HttpStatus.CONFLICT.value());
    }
    
    /**
     * Build internal server error response
     */
    public ApiResponse<Object> buildInternalErrorResponse(String message) {
        return ApiResponse.internalError(message);
    }
    
    /**
     * Build bad request error response
     */
    public ApiResponse<Object> buildBadRequestErrorResponse(String message) {
        return ApiResponse.error(message, HttpStatus.BAD_REQUEST.value());
    }
    
    /**
     * Build method not allowed error response
     */
    public ApiResponse<Object> buildMethodNotAllowedErrorResponse(String method, String[] supportedMethods) {
        String message = String.format("Method '%s' is not allowed. Supported methods: %s", 
                method, String.join(", ", supportedMethods));
        return ApiResponse.error(message, HttpStatus.METHOD_NOT_ALLOWED.value());
    }
    
    /**
     * Build unsupported media type error response
     */
    public ApiResponse<Object> buildUnsupportedMediaTypeErrorResponse(String contentType) {
        String message = String.format("Unsupported media type: %s", contentType);
        return ApiResponse.error(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }
    
    /**
     * Build request entity too large error response
     */
    public ApiResponse<Object> buildRequestEntityTooLargeErrorResponse(long maxSize) {
        String message = String.format("Request entity too large. Maximum size allowed: %d bytes", maxSize);
        return ApiResponse.error(message, HttpStatus.PAYLOAD_TOO_LARGE.value());
    }
    
    /**
     * Build too many requests error response
     */
    public ApiResponse<Object> buildTooManyRequestsErrorResponse(String message) {
        return ApiResponse.error(message, HttpStatus.TOO_MANY_REQUESTS.value());
    }
    
    /**
     * Build service unavailable error response
     */
    public ApiResponse<Object> buildServiceUnavailableErrorResponse(String message) {
        return ApiResponse.error(message, HttpStatus.SERVICE_UNAVAILABLE.value());
    }
    
    /**
     * Build custom error response with specific status code
     */
    public ApiResponse<Object> buildCustomErrorResponse(String message, HttpStatus status) {
        return ApiResponse.error(message, status.value());
    }
    
    /**
     * Build error response with multiple error messages
     */
    public ApiResponse<Object> buildErrorResponseWithMessages(String message, List<String> errors) {
        return ApiResponse.error(message, errors);
    }
    
    /**
     * Build error response with field errors and global errors
     */
    public ApiResponse<Object> buildErrorResponseWithFieldAndGlobalErrors(String message, 
                                                                        Map<String, String> fieldErrors, 
                                                                        List<String> globalErrors) {
        return ApiResponse.error(message, globalErrors, fieldErrors);
    }
    
    /**
     * Build business logic error response
     */
    public ApiResponse<Object> buildBusinessLogicErrorResponse(String operation, String reason) {
        String message = String.format("Business logic error in %s: %s", operation, reason);
        return ApiResponse.error(message, HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
    
    /**
     * Build resource already exists error response
     */
    public ApiResponse<Object> buildResourceAlreadyExistsErrorResponse(String resource, String identifier) {
        String message = String.format("%s already exists with identifier: %s", resource, identifier);
        return ApiResponse.error(message, HttpStatus.CONFLICT.value());
    }
    
    /**
     * Build insufficient permissions error response
     */
    public ApiResponse<Object> buildInsufficientPermissionsErrorResponse(String resource, String action) {
        String message = String.format("Insufficient permissions to %s %s", action, resource);
        return ApiResponse.forbidden(message);
    }
    
    /**
     * Build rate limit exceeded error response
     */
    public ApiResponse<Object> buildRateLimitExceededErrorResponse(String operation, int limit, String timeWindow) {
        String message = String.format("Rate limit exceeded for %s. Maximum %d requests per %s", 
                operation, limit, timeWindow);
        return ApiResponse.error(message, HttpStatus.TOO_MANY_REQUESTS.value());
    }
    
    /**
     * Build maintenance mode error response
     */
    public ApiResponse<Object> buildMaintenanceModeErrorResponse(String message) {
        return ApiResponse.error(message, HttpStatus.SERVICE_UNAVAILABLE.value());
    }
    
    /**
     * Build feature not available error response
     */
    public ApiResponse<Object> buildFeatureNotAvailableErrorResponse(String feature) {
        String message = String.format("Feature '%s' is not available", feature);
        return ApiResponse.error(message, HttpStatus.NOT_IMPLEMENTED.value());
    }
    
    /**
     * Build external service error response
     */
    public ApiResponse<Object> buildExternalServiceErrorResponse(String service, String error) {
        String message = String.format("External service '%s' error: %s", service, error);
        return ApiResponse.error(message, HttpStatus.BAD_GATEWAY.value());
    }
    
    /**
     * Build timeout error response
     */
    public ApiResponse<Object> buildTimeoutErrorResponse(String operation, long timeoutMs) {
        String message = String.format("Operation '%s' timed out after %d milliseconds", operation, timeoutMs);
        return ApiResponse.error(message, HttpStatus.REQUEST_TIMEOUT.value());
    }
    
    /**
     * Build database error response
     */
    public ApiResponse<Object> buildDatabaseErrorResponse(String operation, String error) {
        String message = String.format("Database error during %s: %s", operation, error);
        return ApiResponse.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    
    /**
     * Build cache error response
     */
    public ApiResponse<Object> buildCacheErrorResponse(String operation, String error) {
        String message = String.format("Cache error during %s: %s", operation, error);
        return ApiResponse.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    
    /**
     * Build file operation error response
     */
    public ApiResponse<Object> buildFileOperationErrorResponse(String operation, String fileName, String error) {
        String message = String.format("File operation '%s' failed for '%s': %s", operation, fileName, error);
        return ApiResponse.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    
    /**
     * Build network error response
     */
    public ApiResponse<Object> buildNetworkErrorResponse(String operation, String error) {
        String message = String.format("Network error during %s: %s", operation, error);
        return ApiResponse.error(message, HttpStatus.BAD_GATEWAY.value());
    }
}
