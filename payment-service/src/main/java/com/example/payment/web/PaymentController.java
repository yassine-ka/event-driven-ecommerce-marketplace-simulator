package com.example.payment.web;

import com.example.payment.application.PaymentService;
import com.example.payment.domain.Payment;
import com.example.payment.web.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment API")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID", description = "Retrieves payment information for an order")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") UUID orderId) {
        Optional<Payment> payment = paymentService.getPayment(orderId);
        return payment.map(p -> ResponseEntity.ok(PaymentResponse.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
