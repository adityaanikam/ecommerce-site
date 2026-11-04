package com.ecommerce.catalog.messaging;

import com.ecommerce.catalog.service.StockService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumes OrderPlaced and decrements stock, exactly once per event.
 *
 * How the deduplication works, and why it is in this order:
 *
 *   1. Claim the event by inserting its id into the processed_events inbox. The id is
 *      the primary key, so a second copy of the same event fails that insert and the
 *      method returns without touching stock.
 *   2. Only then decrement.
 *
 * Claiming first means a crash between step 1 and step 2 loses that decrement rather
 * than applying it twice. That is the trade this consumer makes on purpose: for
 * stock, double-decrementing is the worse failure, because it sells inventory the
 * warehouse does not have, while a missed decrement shows up as a stock count that is
 * too high and is caught by a reconciliation job. Doing it the other way round
 * (decrement, then record) would risk the double. Getting true exactly-once here would need
 * the decrement and the inbox write in one transaction, which is possible on a
 * MongoDB replica set but not on the standalone node this runs on locally.
 */
@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);
    private static final String EVENT_TYPE = "OrderPlaced";

    private final ProcessedEventRepository processedEvents;
    private final StockService stockService;

    @KafkaListener(
            topics = "${catalog.kafka.order-placed-topic:order-placed}",
            groupId = "${spring.kafka.consumer.group-id:catalog-service}"
    )
    public void onOrderPlaced(OrderPlacedEvent event,
                              @Header(name = "X-Correlation-Id", required = false) byte[] correlationId) {
        if (correlationId != null) {
            MDC.put("correlationId", new String(correlationId));
        }
        try {
            handle(event);
        } finally {
            MDC.remove("correlationId");
        }
    }

    void handle(OrderPlacedEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) {
            log.warn("Dropping OrderPlaced with no event id, it cannot be deduplicated");
            return;
        }

        if (!claim(event.eventId())) {
            log.info("Ignoring duplicate OrderPlaced {} for order {}", event.eventId(), event.orderId());
            return;
        }

        if (event.items() == null || event.items().isEmpty()) {
            log.warn("OrderPlaced {} for order {} carried no lines", event.eventId(), event.orderId());
            return;
        }

        for (OrderPlacedEvent.Line line : event.items()) {
            if (line.productId() == null || line.quantity() <= 0) {
                log.warn("Skipping malformed line {} in OrderPlaced {}", line, event.eventId());
                continue;
            }
            if (stockService.decrementStock(line.productId(), line.quantity()) == null) {
                // The order was already accepted, so this cannot be undone here. It is
                // logged loudly as an oversell for an operator to reconcile.
                log.error("Oversell on order {}: could not decrement product {} by {}",
                        event.orderId(), line.productId(), line.quantity());
            }
        }
        log.info("Applied OrderPlaced {} for order {}, {} lines",
                event.eventId(), event.orderId(), event.items().size());
    }

    /**
     * @return true when this call is the one that claimed the event, false when it was
     *         already claimed by an earlier delivery.
     */
    private boolean claim(String eventId) {
        try {
            processedEvents.insert(new ProcessedEvent(eventId, EVENT_TYPE, Instant.now()));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}
