package com.example.inventory.application;

import com.example.inventory.domain.event.InventoryReservedEvent;
import com.example.inventory.domain.event.InventoryReservationFailedEvent;
import com.example.inventory.domain.event.OrderCreatedEvent;
import com.example.inventory.domain.event.PaymentFailedEvent;
import com.example.inventory.infrastructure.kafka.InventoryEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles Saga pattern events for inventory service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySagaHandler {

    private final InventoryService inventoryService;
    private final InventoryEventPublisher eventPublisher;

    /**
     * Handles order creation event.
     * Attempts to reserve stock for all items in the order.
     */
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        boolean allReserved = true;
        String failureReason = null;

        // Try to reserve stock for each item
        for (OrderCreatedEvent.OrderItem item : event.items()) {
            boolean reserved = inventoryService.reserveStock(
                    event.orderId(),
                    item.productId(),
                    item.quantity()
            );

            if (!reserved) {
                allReserved = false;
                failureReason = String.format("Insufficient stock for product: %s", item.productName());
                break;
            }
        }

        // Publish appropriate event
        if (allReserved) {
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    event.orderId(),
                    event.customerId(),
                    event.totalAmount(),
                    java.time.LocalDateTime.now()
            );
            eventPublisher.publishInventoryReserved(reservedEvent);
        } else {
            // Release any already reserved items (compensation)
            inventoryService.releaseStock(event.orderId());
            
            InventoryReservationFailedEvent failedEvent = new InventoryReservationFailedEvent(
                    event.orderId(),
                    event.customerId(),
                    failureReason,
                    java.time.LocalDateTime.now()
            );
            eventPublisher.publishInventoryReservationFailed(failedEvent);
        }
    }

    /**
     * Handles payment failure event.
     * Releases reserved stock as compensation.
     */
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Processing compensation for order {}: releasing reserved stock", event.orderId());
        inventoryService.releaseStock(event.orderId());
    }
}
