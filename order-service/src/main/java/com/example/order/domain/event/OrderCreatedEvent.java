package com.example.order.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when an order is created.
 * This event triggers the Saga pattern flow.
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        List<OrderItem> items,
        LocalDateTime timestamp
) {
    public OrderCreatedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items cannot be null or empty");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Represents an order item in the event.
     */
    public record OrderItem(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {
    }
}
