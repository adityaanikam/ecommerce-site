package com.ecommerce.auth.web;

import com.ecommerce.auth.dto.ApiEnvelope;
import com.ecommerce.auth.service.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Turns everything into the {success, data, message, errors} envelope the frontend's
 * getErrorMessage already knows how to read.
 *
 * Validation failures come back as a list the registration form can show per field.
 * Anything unexpected comes back as a flat 500 with the correlation id and nothing
 * else: the stack trace goes to the log, not to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleAuth(AuthException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiEnvelope.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiEnvelope.error("Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ApiEnvelope.error("Request body could not be read"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiEnvelope<Void>> handleEverythingElse(Exception e) {
        String correlationId = MDC.get("correlationId");
        log.error("Unhandled exception [correlationId={}]", correlationId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiEnvelope.error("Something went wrong"
                        + (correlationId == null ? "" : " (correlation id " + correlationId + ")")));
    }
}
