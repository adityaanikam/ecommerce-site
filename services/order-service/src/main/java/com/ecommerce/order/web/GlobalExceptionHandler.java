package com.ecommerce.order.web;

import com.ecommerce.order.client.CatalogUnavailableException;
import com.ecommerce.order.dto.ApiEnvelope;
import com.ecommerce.order.service.OrderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleOrder(OrderException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiEnvelope.error(e.getMessage()));
    }

    /**
     * 503 with Retry-After, not 500. The request was fine; a dependency was not, and
     * retrying in a moment is genuinely the right thing for the caller to do. A 500
     * would tell them the opposite.
     */
    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleCatalogDown(CatalogUnavailableException e) {
        log.warn("Refusing an order because the catalog is unavailable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "10")
                .body(ApiEnvelope.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(ApiEnvelope.error("Validation failed", errors));
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
