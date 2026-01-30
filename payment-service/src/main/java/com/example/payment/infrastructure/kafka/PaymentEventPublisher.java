package com.example.payment.infrastructure.kafka;

import com.example.payment.domain.event.PaymentFailedEvent;
import com.example.payment.domain.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event publisher for payment events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.orderId().toString(), event);
            log.info("Published PaymentProcessedEvent for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent for order: {}", event.orderId(), e);
            throw new RuntimeException("Failed to publish payment processed event", e);
        }
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.orderId().toString(), event);
            log.info("Published PaymentFailedEvent for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailedEvent for order: {}", event.orderId(), e);
            throw new RuntimeException("Failed to publish payment failed event", e);
        }
    }
}
