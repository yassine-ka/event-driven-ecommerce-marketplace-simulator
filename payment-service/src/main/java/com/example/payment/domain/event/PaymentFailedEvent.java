package com.example.payment.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when payment processing fails.
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
