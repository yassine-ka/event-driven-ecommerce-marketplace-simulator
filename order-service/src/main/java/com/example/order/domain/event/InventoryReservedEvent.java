package com.example.order.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published by Inventory Service when stock is successfully reserved.
 * This event triggers the payment processing step in the Saga.
 */
public record InventoryReservedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        LocalDateTime timestamp
) {
    public InventoryReservedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
