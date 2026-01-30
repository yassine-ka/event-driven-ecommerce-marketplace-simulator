package com.example.payment.application;

import com.example.payment.domain.Payment;
import com.example.payment.domain.event.InventoryReservedEvent;
import com.example.payment.domain.event.PaymentFailedEvent;
import com.example.payment.domain.event.PaymentProcessedEvent;
import com.example.payment.infrastructure.kafka.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Handles Saga pattern events for payment service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaHandler {

    private final PaymentService paymentService;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Handles inventory reservation success event.
     * Processes payment and publishes appropriate event.
     */
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        Payment payment = paymentService.processPayment(
                event.orderId(),
                event.customerId(),
                event.totalAmount()
        );

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                    payment.getOrderId(),
                    payment.getCustomerId(),
                    payment.getId(),
                    payment.getAmount(),
                    payment.getProcessedAt()
            );
            eventPublisher.publishPaymentProcessed(processedEvent);
        } else {
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    payment.getOrderId(),
                    payment.getCustomerId(),
                    payment.getFailureReason(),
                    java.time.LocalDateTime.now()
            );
            eventPublisher.publishPaymentFailed(failedEvent);
        }
    }
}
