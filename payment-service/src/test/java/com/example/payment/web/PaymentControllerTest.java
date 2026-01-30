package com.example.payment.web;

import com.example.payment.application.PaymentService;
import com.example.payment.domain.Payment;
import com.example.payment.web.dto.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldGetPaymentByOrderId() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .customerId(customerId)
                .amount(BigDecimal.valueOf(100.00))
                .status(Payment.PaymentStatus.COMPLETED)
                .processedAt(LocalDateTime.now())
                .build();

        when(paymentService.getPayment(orderId)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(paymentService.getPayment(orderId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/payments/order/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnFailedPayment() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100.00))
                .status(Payment.PaymentStatus.FAILED)
                .failureReason("Payment gateway timeout")
                .build();

        when(paymentService.getPayment(orderId)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason").value("Payment gateway timeout"));
    }
}
