package com.example.order.infrastructure.kafka;

import com.example.order.application.OrderSagaHandler;
import com.example.order.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for order-related events from other services.
 * Handles events from Inventory and Payment services to drive the Saga pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderSagaHandler sagaHandler;
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @KafkaListener(
            topics = INVENTORY_EVENTS_TOPIC,
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryReservedEvent(
            @Payload InventoryReservedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received InventoryReservedEvent for order: {}", event.orderId());
            sagaHandler.handleInventoryReserved(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent for order: {}", event.orderId(), e);
            // In production, consider dead letter queue or retry mechanism
        }
    }

    @KafkaListener(
            topics = INVENTORY_EVENTS_TOPIC,
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryReservationFailedEvent(
            @Payload InventoryReservationFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received InventoryReservationFailedEvent for order: {}", event.orderId());
            sagaHandler.handleInventoryReservationFailed(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing InventoryReservationFailedEvent for order: {}", event.orderId(), e);
        }
    }

    @KafkaListener(
            topics = PAYMENT_EVENTS_TOPIC,
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentProcessedEvent(
            @Payload PaymentProcessedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received PaymentProcessedEvent for order: {}", event.orderId());
            sagaHandler.handlePaymentProcessed(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent for order: {}", event.orderId(), e);
        }
    }

    @KafkaListener(
            topics = PAYMENT_EVENTS_TOPIC,
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailedEvent(
            @Payload PaymentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received PaymentFailedEvent for order: {}", event.orderId());
            sagaHandler.handlePaymentFailed(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent for order: {}", event.orderId(), e);
        }
    }
}
