package com.example.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when an order is cancelled.
 * This can happen due to inventory reservation failure or payment failure.
 */
public record OrderCancelledEvent(
        UUID orderId,
        UUID customerId,
        String reason,
        LocalDateTime timestamp
) {
    public OrderCancelledEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
