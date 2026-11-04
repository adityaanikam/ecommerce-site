package com.ecommerce.catalog.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The OrderPlaced event as this service chooses to read it.
 *
 * This is a local copy of the contract, not an import of order-service's class. That
 * is the point of not having a shared domain library: order-service can add fields to
 * the event without this service needing to be rebuilt, and @JsonIgnoreProperties
 * means unknown fields are dropped rather than throwing. The only fields catalog
 * actually needs are the event id, for deduplication, and the lines, to decrement.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPlacedEvent(
        String eventId,
        String orderId,
        List<Line> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Line(String productId, int quantity) {
    }
}
