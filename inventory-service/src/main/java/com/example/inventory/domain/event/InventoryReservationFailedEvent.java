package com.example.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when stock reservation fails (out of stock).
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
