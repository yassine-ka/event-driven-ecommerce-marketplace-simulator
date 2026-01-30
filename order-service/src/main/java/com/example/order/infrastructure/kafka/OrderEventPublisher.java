package com.example.order.infrastructure.kafka;

import com.example.order.domain.Order;
import com.example.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Kafka event publisher for order events.
 * Publishes events to Kafka topics to drive the Saga pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    /**
     * Publishes OrderCreatedEvent to Kafka.
     * This event initiates the Saga pattern flow.
     */
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItem(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .collect(Collectors.toList()),
                order.getCreatedAt()
        );

        try {
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getId().toString(), event);
            log.info("Published OrderCreatedEvent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to publish order created event", e);
        }
    }
}
