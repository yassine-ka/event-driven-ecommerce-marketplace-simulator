package com.example.order.application;

import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.domain.event.*;
import com.example.order.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderSagaHandler sagaHandler;

    private UUID orderId;
    private UUID customerId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void shouldUpdateOrderToProcessingWhenInventoryReserved() {
        // Given
        InventoryReservedEvent event = new InventoryReservedEvent(
                orderId,
                customerId,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        sagaHandler.handleInventoryReserved(event);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldCancelOrderWhenInventoryReservationFails() {
        // Given
        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(
                orderId,
                customerId,
                "Out of stock",
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        sagaHandler.handleInventoryReservationFailed(event);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldCompleteOrderWhenPaymentProcessed() {
        // Given
        order.setStatus(OrderStatus.PROCESSING);
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderId,
                customerId,
                UUID.randomUUID(),
                BigDecimal.valueOf(100.00),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        sagaHandler.handlePaymentProcessed(event);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldCancelOrderWhenPaymentFails() {
        // Given
        order.setStatus(OrderStatus.PROCESSING);
        PaymentFailedEvent event = new PaymentFailedEvent(
                orderId,
                customerId,
                "Payment gateway timeout",
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        sagaHandler.handlePaymentFailed(event);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldIgnoreInventoryReservedIfOrderNotPending() {
        // Given
        order.setStatus(OrderStatus.COMPLETED);
        InventoryReservedEvent event = new InventoryReservedEvent(
                orderId,
                customerId,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        sagaHandler.handleInventoryReserved(event);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        InventoryReservedEvent event = new InventoryReservedEvent(
                orderId,
                customerId,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> sagaHandler.handleInventoryReserved(event)
        );
    }
}
