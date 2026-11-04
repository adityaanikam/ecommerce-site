package com.ecommerce.order.repository;

import com.ecommerce.order.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, IdempotencyRecord.Key> {
}
