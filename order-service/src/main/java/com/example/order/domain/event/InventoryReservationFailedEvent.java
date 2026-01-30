package com.example.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published by Inventory Service when stock reservation fails (out of stock).
 * This event causes the order to be cancelled.
 */
public record InventoryReservationFailedEvent(
        UUID orderId,
        UUID customerId,
        String reason,
        LocalDateTime timestamp
) {
    public InventoryReservationFailedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
