package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Scoped by user as well as by id, so a request for someone else's order id is a
     * 404 rather than a leak. Authorisation enforced by the query, not by a check
     * that could be forgotten at a call site.
     */
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
}
