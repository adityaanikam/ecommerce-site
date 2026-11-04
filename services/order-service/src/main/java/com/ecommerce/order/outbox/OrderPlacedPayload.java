package com.ecommerce.order.outbox;

import java.util.List;

/**
 * The OrderPlaced message, as this service publishes it.
 *
 * catalog-service has its own copy of this shape with only the fields it cares
 * about and @JsonIgnoreProperties(ignoreUnknown = true), so a field added here does
 * not require catalog to be rebuilt and deployed in lockstep. That independence is
 * the reason there is no shared domain library, and the cost is this duplication,
 * which is deliberate.
 */
public record OrderPlacedPayload(
        String eventId,
        String orderId,
        String userId,
        String occurredAt,
        List<Line> items
) {
    public record Line(String productId, int quantity) {
    }
}
