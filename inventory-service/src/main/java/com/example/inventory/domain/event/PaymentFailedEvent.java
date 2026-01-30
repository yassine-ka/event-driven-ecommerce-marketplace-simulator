package com.example.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received from Payment Service when payment fails.
 * Triggers compensation: release reserved stock.
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
