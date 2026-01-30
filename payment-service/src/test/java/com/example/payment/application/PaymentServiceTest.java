package com.example.payment.application;

import com.example.payment.domain.Payment;
import com.example.payment.infrastructure.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        amount = BigDecimal.valueOf(100.00);
        ReflectionTestUtils.setField(paymentService, "failureRate", 0.0); // Set to 0% for predictable tests
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        Payment result = paymentService.processPayment(orderId, customerId, amount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
        verify(paymentRepository, times(2)).save(any(Payment.class)); // Initial save + status update save
    }

    @Test
    void shouldReturnExistingPaymentIfAlreadyProcessed() {
        // Given
        Payment existingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));

        // When
        Payment result = paymentService.processPayment(orderId, customerId, amount);

        // Then
        assertThat(result).isEqualTo(existingPayment);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldFailPaymentWithHighFailureRate() {
        // Given
        ReflectionTestUtils.setField(paymentService, "failureRate", 1.0); // 100% failure rate

        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.FAILED)
                .failureReason("Payment gateway timeout - simulated failure")
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        Payment result = paymentService.processPayment(orderId, customerId, amount);

        // Then
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).isNotNull();
    }

    @Test
    void shouldGetPaymentByOrderId() {
        // Given
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // When
        Optional<Payment> result = paymentService.getPayment(orderId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        assertThat(result.get().getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    void shouldReturnEmptyWhenPaymentNotFound() {
        // Given
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.getPayment(orderId);

        // Then
        assertThat(result).isEmpty();
    }
}
