package com.example.order.application;

import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.infrastructure.kafka.OrderEventPublisher;
import com.example.order.infrastructure.repository.OrderRepository;
import com.example.order.web.dto.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;
    private UUID customerId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
        item.setProductId(productId);
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(50.00));

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(customerId);
        createOrderRequest.setItems(java.util.List.of(item));
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        // When
        var result = orderService.createOrder(createOrderRequest, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishOrderCreated(any(Order.class));
    }

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyExists() {
        // Given
        String idempotencyKey = "test-key-123";
        Order existingOrder = Order.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .idempotencyKey(idempotencyKey)
                .build();

        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingOrder));

        // When
        var result = orderService.createOrder(createOrderRequest, idempotencyKey);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingOrder.getId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishOrderCreated(any(Order.class));
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        CreateOrderRequest.OrderItemDto item1 = new CreateOrderRequest.OrderItemDto();
        item1.setProductId(UUID.randomUUID());
        item1.setProductName("Product 1");
        item1.setQuantity(2);
        item1.setUnitPrice(BigDecimal.valueOf(25.50));

        CreateOrderRequest.OrderItemDto item2 = new CreateOrderRequest.OrderItemDto();
        item2.setProductId(UUID.randomUUID());
        item2.setProductName("Product 2");
        item2.setQuantity(3);
        item2.setUnitPrice(BigDecimal.valueOf(10.00));

        createOrderRequest.setItems(java.util.List.of(item1, item2));

        UUID orderId = UUID.randomUUID();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        // When
        var result = orderService.createOrder(createOrderRequest, null);

        // Then
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(81.00)); // (2 * 25.50) + (3 * 10.00) = 51.00 + 30.00 = 81.00
    }

    @Test
    void shouldGetOrderById() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .status(OrderStatus.COMPLETED)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        var result = orderService.getOrder(orderId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderId);
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        var result = orderService.getOrder(orderId);

        // Then
        assertThat(result).isEmpty();
    }
}
