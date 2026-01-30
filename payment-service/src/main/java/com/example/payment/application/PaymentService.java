package com.example.payment.application;

import com.example.payment.domain.Payment;
import com.example.payment.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Application service for payment processing.
 * Simulates payment processing with a configurable failure rate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @Value("${payment.failure-rate:0.2}")
    private double failureRate;

    /**
     * Processes a payment for an order.
     * Simulates payment processing with a chance of failure.
     */
    @Transactional
    public Payment processPayment(UUID orderId, UUID customerId, BigDecimal amount) {
        // Check if payment already exists
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            log.info("Payment already exists for order: {}", orderId);
            return existingPayment.get();
        }

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.PROCESSING)
                .build();
        payment = paymentRepository.save(payment);

        // Simulate payment processing
        try {
            Thread.sleep(100); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate failure (20% chance by default)
        if (random.nextDouble() < failureRate) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway timeout - simulated failure");
            payment = paymentRepository.save(payment);
            log.warn("Payment failed for order {}: {}", orderId, payment.getFailureReason());
        } else {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setProcessedAt(java.time.LocalDateTime.now());
            payment = paymentRepository.save(payment);
            log.info("Payment processed successfully for order {}: {}", orderId, amount);
        }

        return payment;
    }

    public Optional<Payment> getPayment(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
