package com.ecommerce.order.outbox;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Exposes the outbox backlog as a gauge.
 *
 * This is the single most important number to watch in this design. The outbox makes
 * publishing eventually consistent, and the failure that matters is not an error in
 * a log, it is the backlog quietly growing because the broker is unreachable or the
 * publisher thread has stopped. Orders keep succeeding, customers see nothing wrong,
 * and stock silently stops being decremented. A gauge on unpublished rows is what
 * turns that into an alert.
 *
 * The count is refreshed on the publisher's own schedule rather than inside the
 * gauge callback, so Prometheus scraping does not fire a COUNT query against the
 * orders database.
 */
@Component
@RequiredArgsConstructor
public class OutboxMetrics {

    private static final Logger log = LoggerFactory.getLogger(OutboxMetrics.class);

    private final OutboxEventRepository outbox;
    private final MeterRegistry meterRegistry;

    private final AtomicLong pending = new AtomicLong(0);

    @PostConstruct
    void register() {
        Gauge.builder("order_outbox_pending_events", pending, AtomicLong::get)
                .description("Outbox events written but not yet published to Kafka")
                .register(meterRegistry);
    }

    /**
     * Called by the publisher after each round, so the gauge tracks the real backlog
     * without adding a query of its own.
     */
    void refresh() {
        try {
            pending.set(outbox.countByPublishedAtIsNull());
        } catch (Exception e) {
            log.debug("Could not refresh the outbox backlog gauge: {}", e.getMessage());
        }
    }
}
