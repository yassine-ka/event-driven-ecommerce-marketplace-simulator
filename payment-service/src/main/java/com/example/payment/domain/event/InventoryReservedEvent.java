package com.example.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received from Inventory Service when stock is reserved.
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
