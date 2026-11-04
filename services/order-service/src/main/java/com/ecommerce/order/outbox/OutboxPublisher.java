package com.ecommerce.order.outbox;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The second half of the transactional outbox: a tick that drains the table into
 * Kafka. The actual work, and its transaction, live in OutboxBatchPublisher.
 *
 * Publishing is scheduled rather than triggered by the write, so a broker that was
 * down when the order was placed costs nothing more than a delay: the row is still
 * there and the next tick picks it up.
 *
 * The whole design is at-least-once. A crash between the broker acknowledging the
 * message and this transaction committing leaves the row unpublished, and it is sent
 * again. That duplicate is expected and carries the same event id, which is what
 * catalog-service's inbox deduplicates on. Making it exactly-once would mean a
 * distributed transaction across PostgreSQL and Kafka; at-least-once plus a
 * deduplicating consumer is the standard trade and far easier to reason about.
 */
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxBatchPublisher batchPublisher;

    @Scheduled(fixedDelayString = "${order.outbox.poll-interval-ms:1000}")
    public void publishPending() {
        try {
            int published = batchPublisher.publishBatch();
            if (published > 0) {
                log.info("Published {} outbox events", published);
            }
        } catch (Exception e) {
            // Never let the scheduler's thread die on an exception, or publishing
            // stops silently and orders pile up unsent.
            log.error("Outbox publish round failed, will retry on the next tick", e);
        }
    }
}
