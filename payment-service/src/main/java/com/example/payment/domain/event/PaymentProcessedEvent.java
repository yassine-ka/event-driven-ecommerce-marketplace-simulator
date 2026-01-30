package com.example.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when payment is successfully processed.
 */
public record PaymentProcessedEvent(
        UUID orderId,
        UUID customerId,
        UUID paymentId,
        BigDecimal amount,
        LocalDateTime timestamp
) {
    public PaymentProcessedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
