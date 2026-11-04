package com.ecommerce.order.service;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.dto.OrderDtos;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates placing an order. The database work lives in OrderWriter, which is a
 * separate bean so that its @Transactional boundary is real rather than bypassed by
 * self-invocation.
 *
 * The sequence, and why it is this sequence:
 *
 *  1. If this user has already used this Idempotency-Key, return the order it
 *     produced and stop. A retried request must never create a second order.
 *  2. Ask catalog-service for the real price and stock of every line. This happens
 *     OUTSIDE any transaction: holding a database transaction open across a network
 *     call to another service ties up a pooled connection for the duration of that
 *     call, and under load that is how the pool runs dry.
 *  3. Write the order, its lines, the outbox event and the idempotency record in
 *     one transaction.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderWriter writer;
    private final CatalogClient catalogClient;

    public OrderDtos.OrderView placeOrder(UUID userId, UUID idempotencyKey,
                                          OrderDtos.CreateOrderRequest request) {
        String requestHash = fingerprint(request);

        Optional<OrderDtos.OrderView> replayed = writer.replay(userId, idempotencyKey, requestHash);
        if (replayed.isPresent()) {
            log.info("Idempotency-Key {} replayed for user {}, returning the existing order",
                    idempotencyKey, userId);
            return replayed.get();
        }

        rejectDuplicateProducts(request.items());

        Map<String, CatalogClient.StockView> catalog = catalogClient.lookup(
                request.items().stream().map(OrderDtos.Line::productId).toList());

        try {
            return writer.createOrder(userId, idempotencyKey, requestHash, request, catalog);
        } catch (DataIntegrityViolationException e) {
            // Two copies of the same request arrived close enough together that both
            // got past the replay check. The primary key on
            // (user_id, idempotency_key) is what stopped the second from inserting,
            // so re-read and return the first one's order. The check alone cannot
            // cover this; the constraint is what actually guarantees one order.
            log.info("Concurrent request with Idempotency-Key {} for user {}, returning the stored order",
                    idempotencyKey, userId);
            return writer.replay(userId, idempotencyKey, requestHash).orElseThrow(() -> e);
        }
    }

    public OrderDtos.OrderView findForUser(UUID userId, UUID orderId) {
        return writer.findForUser(userId, orderId);
    }

    public OrderDtos.OrderListView listForUser(UUID userId, int page, int size) {
        return writer.listForUser(userId, page, size);
    }

    private static void rejectDuplicateProducts(List<OrderDtos.Line> lines) {
        Set<String> seen = new HashSet<>();
        for (OrderDtos.Line line : lines) {
            if (!seen.add(line.productId())) {
                throw OrderException.duplicateProductLines();
            }
        }
    }

    /**
     * Identifies "the same request" for idempotency purposes.
     *
     * Only the parts that decide what is being bought and where it goes are
     * included, so a retry that differs in something cosmetic still matches, while
     * a retry that quietly changes the basket does not.
     */
    String fingerprint(OrderDtos.CreateOrderRequest request) {
        StringBuilder canonical = new StringBuilder();
        request.items().stream()
                .sorted((a, b) -> a.productId().compareTo(b.productId()))
                .forEach(line -> canonical.append(line.productId()).append('x')
                        .append(line.quantity()).append(';'));
        OrderDtos.Address shipping = request.shippingAddress();
        canonical.append('|').append(shipping.address()).append('|').append(shipping.city())
                .append('|').append(shipping.zipCode()).append('|').append(shipping.country())
                .append('|').append(request.paymentMethod().type());
        return sha256(canonical.toString());
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
