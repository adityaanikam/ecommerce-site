package com.ecommerce.order.model;

/**
 * Mirrored by a CHECK constraint in V1, and by the status union the frontend's
 * OrderDto already declares.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
