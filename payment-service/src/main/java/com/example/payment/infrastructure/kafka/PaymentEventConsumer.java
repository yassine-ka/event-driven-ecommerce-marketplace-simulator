package com.example.payment.infrastructure.kafka;

import com.example.payment.application.PaymentSagaHandler;
import com.example.payment.domain.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for payment service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentSagaHandler sagaHandler;
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    @KafkaListener(
            topics = INVENTORY_EVENTS_TOPIC,
            groupId = "payment-service-group",
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
        }
    }
}
