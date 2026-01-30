package com.example.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when an order is successfully completed.
 * This is the final event in the happy path of the Saga.
 */
public record OrderCompletedEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime timestamp
) {
    public OrderCompletedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
