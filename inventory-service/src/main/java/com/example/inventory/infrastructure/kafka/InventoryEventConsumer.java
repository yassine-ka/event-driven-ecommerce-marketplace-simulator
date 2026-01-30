package com.example.inventory.infrastructure.kafka;

import com.example.inventory.application.InventorySagaHandler;
import com.example.inventory.domain.event.OrderCreatedEvent;
import com.example.inventory.domain.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for inventory service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventorySagaHandler sagaHandler;
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @KafkaListener(
            topics = ORDER_EVENTS_TOPIC,
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received OrderCreatedEvent for order: {}", event.orderId());
            sagaHandler.handleOrderCreated(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for order: {}", event.orderId(), e);
        }
    }

    @KafkaListener(
            topics = PAYMENT_EVENTS_TOPIC,
            groupId = "inventory-service-group",
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
