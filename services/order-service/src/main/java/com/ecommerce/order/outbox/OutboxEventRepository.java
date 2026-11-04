package com.ecommerce.order.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * The publisher's claim query.
     *
     * PESSIMISTIC_WRITE with SKIP LOCKED means two instances of order-service polling
     * at the same moment take different rows rather than both taking the same one and
     * publishing it twice. Without it, horizontal scaling would turn at-least-once
     * into reliably-twice. SKIP LOCKED rather than plain FOR UPDATE so the second
     * instance moves on instead of blocking behind the first.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT e FROM OutboxEvent e WHERE e.publishedAt IS NULL ORDER BY e.createdAt ASC")
    List<OutboxEvent> claimUnpublished(Pageable pageable);

    long countByPublishedAtIsNull();
}
