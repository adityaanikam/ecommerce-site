package com.ecommerce.order.service;

import org.springframework.http.HttpStatus;

public class OrderException extends RuntimeException {

    private final HttpStatus status;

    public OrderException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static OrderException notAuthenticated() {
        return new OrderException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    public static OrderException orderNotFound() {
        return new OrderException(HttpStatus.NOT_FOUND, "Order not found");
    }

    public static OrderException unknownProduct(String productId) {
        return new OrderException(HttpStatus.BAD_REQUEST, "Unknown product: " + productId);
    }

    public static OrderException outOfStock(String productName, int requested, int available) {
        return new OrderException(HttpStatus.CONFLICT,
                "Not enough stock for " + productName + ": asked for " + requested
                        + ", only " + available + " left");
    }

    /**
     * 422 rather than 409: the key is fine, the body is the problem. Reusing a key
     * with a different basket is a client bug, and quietly returning the first order
     * would hide it.
     */
    public static OrderException idempotencyKeyReused() {
        return new OrderException(HttpStatus.UNPROCESSABLE_ENTITY,
                "This Idempotency-Key was already used for a different request body");
    }

    public static OrderException badIdempotencyKey() {
        return new OrderException(HttpStatus.BAD_REQUEST,
                "Idempotency-Key must be a UUID");
    }

    public static OrderException duplicateProductLines() {
        return new OrderException(HttpStatus.BAD_REQUEST,
                "The same product appears more than once; combine the quantities instead");
    }
}
