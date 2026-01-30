package com.example.order.application;

import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.domain.event.*;
import com.example.order.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles Saga pattern events from other services.
 * Manages order state transitions based on events from Inventory and Payment services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaHandler {

    private final OrderRepository orderRepository;

    /**
     * Handles inventory reservation success.
     * Updates order status to PROCESSING and triggers payment processing.
     */
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        Order order = findOrderOrThrow(event.orderId());
        
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            log.info("Order {} status updated to PROCESSING after inventory reservation", order.getId());
        } else {
            log.warn("Order {} is not in PENDING state, ignoring InventoryReservedEvent. Current status: {}", 
                    order.getId(), order.getStatus());
        }
    }

    /**
     * Handles inventory reservation failure.
     * Cancels the order (no compensation needed as no stock was reserved).
     */
    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        Order order = findOrderOrThrow(event.orderId());
        
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} cancelled due to inventory reservation failure: {}", 
                    order.getId(), event.reason());
        } else {
            log.warn("Order {} is not in PENDING state, ignoring InventoryReservationFailedEvent. Current status: {}", 
                    order.getId(), order.getStatus());
        }
    }

    /**
     * Handles payment processing success.
     * Completes the order (final state in happy path).
     */
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        Order order = findOrderOrThrow(event.orderId());
        
        if (order.getStatus() == OrderStatus.PROCESSING) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("Order {} completed successfully after payment processing", order.getId());
        } else {
            log.warn("Order {} is not in PROCESSING state, ignoring PaymentProcessedEvent. Current status: {}", 
                    order.getId(), order.getStatus());
        }
    }

    /**
     * Handles payment processing failure.
     * Cancels the order. Compensation (inventory release) is handled by Inventory Service.
     */
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = findOrderOrThrow(event.orderId());
        
        if (order.getStatus() == OrderStatus.PROCESSING) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} cancelled due to payment failure: {}. Compensation will be handled by Inventory Service.", 
                    order.getId(), event.reason());
        } else {
            log.warn("Order {} is not in PROCESSING state, ignoring PaymentFailedEvent. Current status: {}", 
                    order.getId(), order.getStatus());
        }
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
