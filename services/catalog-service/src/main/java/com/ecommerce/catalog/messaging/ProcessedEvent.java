package com.ecommerce.catalog.messaging;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * The consumer-side inbox: one row per event id this service has already handled.
 *
 * Kafka gives at-least-once delivery, so the same OrderPlaced can arrive twice (a
 * rebalance, a redelivery after a crash between handling and committing the offset).
 * The event id is the primary key here, so the second arrival collides and is
 * dropped. Without this, a duplicate event would decrement stock twice.
 */
@Document(collection = "processed_events")
public class ProcessedEvent {

    @Id
    private String eventId;

    private String eventType;

    /** Only for operator visibility and the TTL below; not used for deduplication. */
    @Indexed(expireAfter = "30d")
    private Instant processedAt;

    public ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, String eventType, Instant processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
