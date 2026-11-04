package com.ecommerce.order.controller;

import com.ecommerce.order.dto.ApiEnvelope;
import com.ecommerce.order.dto.OrderDtos;
import com.ecommerce.order.service.OrderException;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Served at the root; the gateway owns /api and strips it.
 *
 * Every route here requires an authenticated caller, and the gateway has already
 * rejected anything without a valid token before it reaches this service. X-User-Id
 * is therefore trustworthy: the gateway sets it from the verified token and strips
 * any copy the client tried to send. This service never looks at the Authorization
 * header and has no users table of its own.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiEnvelope<OrderDtos.OrderView>> placeOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @Valid @RequestBody OrderDtos.CreateOrderRequest request) {

        UUID userId = requireUserId(userIdHeader);
        UUID idempotencyKey = parseIdempotencyKey(idempotencyKeyHeader);

        OrderDtos.OrderView order = orderService.placeOrder(userId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.ok(order, "Order placed"));
    }

    @GetMapping
    public ResponseEntity<ApiEnvelope<OrderDtos.OrderListView>> listOrders(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiEnvelope.ok(
                orderService.listForUser(requireUserId(userIdHeader), page, Math.min(size, 100))));
    }

    /**
     * The frontend's OrderService.getUserOrders calls /orders/user, so that path is
     * served as well as the plain collection.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiEnvelope<OrderDtos.OrderListView>> listMyOrders(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return listOrders(userIdHeader, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiEnvelope<OrderDtos.OrderView>> getOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable String id) {
        UUID userId = requireUserId(userIdHeader);
        UUID orderId;
        try {
            orderId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // A malformed id is simply not an order this user has.
            throw OrderException.orderNotFound();
        }
        return ResponseEntity.ok(ApiEnvelope.ok(orderService.findForUser(userId, orderId)));
    }

    private static UUID requireUserId(String header) {
        if (header == null || header.isBlank()) {
            throw OrderException.notAuthenticated();
        }
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw OrderException.notAuthenticated();
        }
    }

    /**
     * The header is optional in the sense that a client that omits it still gets an
     * order; it just gets no protection against its own retries. A fresh random key
     * stands in, so the code path is identical either way.
     */
    private static UUID parseIdempotencyKey(String header) {
        if (header == null || header.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            throw OrderException.badIdempotencyKey();
        }
    }
}
