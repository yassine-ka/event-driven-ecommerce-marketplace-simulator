package com.example.order.infrastructure.repository;

import com.example.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Order entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Find an order by idempotency key to prevent duplicate orders.
     */
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
