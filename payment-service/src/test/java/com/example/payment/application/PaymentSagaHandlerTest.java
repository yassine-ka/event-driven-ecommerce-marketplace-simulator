package com.example.payment.application;

import com.example.payment.domain.Payment;
import com.example.payment.domain.event.InventoryReservedEvent;
import com.example.payment.domain.event.PaymentFailedEvent;
import com.example.payment.domain.event.PaymentProcessedEvent;
import com.example.payment.infrastructure.kafka.PaymentEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSagaHandlerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentSagaHandler sagaHandler;

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        totalAmount = BigDecimal.valueOf(150.00);
    }

    @Test
    void shouldProcessPaymentAndPublishProcessedEvent() {
        // Given
        InventoryReservedEvent event = new InventoryReservedEvent(
                orderId,
                customerId,
                totalAmount,
                LocalDateTime.now()
        );

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(totalAmount)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();

        ReflectionTestUtils.setField(paymentService, "failureRate", 0.0); // Ensure success
        when(paymentService.processPayment(orderId, customerId, totalAmount)).thenReturn(payment);

        // When
        sagaHandler.handleInventoryReserved(event);

        // Then
        verify(paymentService, times(1)).processPayment(orderId, customerId, totalAmount);
        verify(eventPublisher, times(1)).publishPaymentProcessed(any(PaymentProcessedEvent.class));
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void shouldProcessPaymentAndPublishFailedEvent() {
        // Given
        InventoryReservedEvent event = new InventoryReservedEvent(
                orderId,
                customerId,
                totalAmount,
                LocalDateTime.now()
        );

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(totalAmount)
                .status(Payment.PaymentStatus.FAILED)
                .failureReason("Payment gateway timeout")
                .build();

        ReflectionTestUtils.setField(paymentService, "failureRate", 1.0); // Ensure failure
        when(paymentService.processPayment(orderId, customerId, totalAmount)).thenReturn(payment);

        // When
        sagaHandler.handleInventoryReserved(event);

        // Then
        verify(paymentService, times(1)).processPayment(orderId, customerId, totalAmount);
        verify(eventPublisher, never()).publishPaymentProcessed(any());
        verify(eventPublisher, times(1)).publishPaymentFailed(any(PaymentFailedEvent.class));
    }
}
