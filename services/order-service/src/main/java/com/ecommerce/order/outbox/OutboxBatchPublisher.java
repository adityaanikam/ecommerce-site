package com.ecommerce.order.outbox;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Publishes one batch of outbox rows, in one transaction.
 *
 * This is a separate bean from OutboxPublisher on purpose. @Transactional and the
 * pessimistic lock on claimUnpublished only take effect through the Spring proxy,
 * and a call from inside the same bean does not go through it. With both methods on
 * one class the scheduled tick failed every single time with "no transaction in
 * progress", silently, and orders sat unpublished for ever while the service looked
 * healthy. Two beans is what makes the proxy apply.
 */
@Component
@RequiredArgsConstructor
public class OutboxBatchPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxBatchPublisher.class);

    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${order.outbox.topic:order-placed}")
    private String topic;

    @Value("${order.outbox.batch-size:50}")
    private int batchSize;

    @Value("${order.outbox.send-timeout-seconds:10}")
    private long sendTimeoutSeconds;

    /**
     * The rows are claimed FOR UPDATE SKIP LOCKED, so a second instance of
     * order-service polling at the same moment takes different rows instead of
     * publishing the same ones twice.
     */
    @Transactional
    public int publishBatch() {
        List<OutboxEvent> pending = outbox.claimUnpublished(PageRequest.of(0, batchSize));
        if (pending.isEmpty()) {
            return 0;
        }
        int published = 0;
        for (OutboxEvent event : pending) {
            if (publish(event)) {
                published++;
            }
        }
        return published;
    }

    private boolean publish(OutboxEvent event) {
        String previousCorrelationId = MDC.get("correlationId");
        if (event.getCorrelationId() != null) {
            MDC.put("correlationId", event.getCorrelationId());
        }
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    // Keyed by order id, so every event for one order lands on the
                    // same partition and keeps its relative order.
                    event.getAggregateId(),
                    event.getPayload());
            record.headers().add("X-Correlation-Id",
                    (event.getCorrelationId() == null ? "" : event.getCorrelationId())
                            .getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));

            // Blocking on the acknowledgement on purpose: the row must not be marked
            // published until the broker has actually taken the message.
            kafkaTemplate.send(record).get(sendTimeoutSeconds, TimeUnit.SECONDS);

            event.setPublishedAt(Instant.now());
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(null);
            outbox.save(event);
            return true;
        } catch (Exception e) {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(truncate(e.getMessage()));
            outbox.save(event);
            log.warn("Could not publish outbox event {} (attempt {}): {}",
                    event.getId(), event.getAttempts(), e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        } finally {
            if (previousCorrelationId == null) {
                MDC.remove("correlationId");
            } else {
                MDC.put("correlationId", previousCorrelationId);
            }
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
