package com.ecommerce.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Ties an Idempotency-Key to the order it created.
 *
 * The key is scoped to the user, not global, so two clients picking the same key
 * cannot collide and nobody can read somebody else's order by guessing one.
 */
@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @EmbeddedId
    private Key id;

    /**
     * A hash of the request body. A retry that reuses a key but changes the basket
     * is a client bug; answering it with the first order would hide that, so it is
     * rejected with a 422 instead.
     */
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "idempotency_key", nullable = false)
        private UUID idempotencyKey;
    }
}
