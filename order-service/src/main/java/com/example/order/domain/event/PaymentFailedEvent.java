package com.example.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published by Payment Service when payment processing fails.
 * This event triggers compensation (inventory release) and cancels the order.
 */
public record PaymentFailedEvent(
        UUID orderId,
        UUID customerId,
        String reason,
        LocalDateTime timestamp
) {
    public PaymentFailedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
