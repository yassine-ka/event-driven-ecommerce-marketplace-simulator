package com.example.inventory.infrastructure.kafka;

import com.example.inventory.domain.event.InventoryReservedEvent;
import com.example.inventory.domain.event.InventoryReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event publisher for inventory events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    public void publishInventoryReserved(InventoryReservedEvent event) {
        try {
            kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.orderId().toString(), event);
            log.info("Published InventoryReservedEvent for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to publish InventoryReservedEvent for order: {}", event.orderId(), e);
            throw new RuntimeException("Failed to publish inventory reserved event", e);
        }
    }

    public void publishInventoryReservationFailed(InventoryReservationFailedEvent event) {
        try {
            kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.orderId().toString(), event);
            log.info("Published InventoryReservationFailedEvent for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to publish InventoryReservationFailedEvent for order: {}", event.orderId(), e);
            throw new RuntimeException("Failed to publish inventory reservation failed event", e);
        }
    }
}
