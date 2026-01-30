package com.example.order.application;

import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.infrastructure.kafka.OrderEventPublisher;
import com.example.order.infrastructure.repository.OrderRepository;
import com.example.order.web.dto.CreateOrderRequest;
import com.example.order.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for order operations.
 * Handles order creation and retrieval, and coordinates with the event publisher.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    /**
     * Creates a new order and publishes OrderCreatedEvent to initiate the Saga.
     * Supports idempotency via idempotency-key header.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) {
        // Check idempotency
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder.isPresent()) {
                log.info("Duplicate order request detected with idempotency key: {}", idempotencyKey);
                return OrderResponse.from(existingOrder.get());
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order entity
        List<Order.OrderItem> orderItems = request.getItems().stream()
                .map(item -> Order.OrderItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .items(orderItems)
                .idempotencyKey(idempotencyKey)
                .build();

        // Save order
        order = orderRepository.save(order);
        log.info("Order created: {}", order.getId());

        // Publish event to initiate Saga
        eventPublisher.publishOrderCreated(order);

        return OrderResponse.from(order);
    }

    /**
     * Retrieves an order by ID.
     */
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from);
    }
}
