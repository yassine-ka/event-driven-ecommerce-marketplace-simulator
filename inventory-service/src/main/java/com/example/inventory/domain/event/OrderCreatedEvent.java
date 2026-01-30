package com.example.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event received from Order Service when an order is created.
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        List<OrderItem> items,
        LocalDateTime timestamp
) {
    public record OrderItem(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {
    }
}
