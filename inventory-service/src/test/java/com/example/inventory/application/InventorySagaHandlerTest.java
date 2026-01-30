package com.example.inventory.application;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.event.InventoryReservedEvent;
import com.example.inventory.domain.event.InventoryReservationFailedEvent;
import com.example.inventory.domain.event.OrderCreatedEvent;
import com.example.inventory.domain.event.PaymentFailedEvent;
import com.example.inventory.infrastructure.kafka.InventoryEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventorySagaHandlerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventorySagaHandler sagaHandler;

    private UUID orderId;
    private UUID customerId;
    private UUID productId1;
    private UUID productId2;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();
    }

    @Test
    void shouldReserveStockAndPublishReservedEvent() {
        // Given
        OrderCreatedEvent.OrderItem item1 = new OrderCreatedEvent.OrderItem(
                productId1, "Product 1", 2, BigDecimal.valueOf(50.00)
        );
        OrderCreatedEvent.OrderItem item2 = new OrderCreatedEvent.OrderItem(
                productId2, "Product 2", 1, BigDecimal.valueOf(30.00)
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                customerId,
                BigDecimal.valueOf(130.00),
                List.of(item1, item2),
                LocalDateTime.now()
        );

        when(inventoryService.reserveStock(orderId, productId1, 2)).thenReturn(true);
        when(inventoryService.reserveStock(orderId, productId2, 1)).thenReturn(true);

        // When
        sagaHandler.handleOrderCreated(event);

        // Then
        verify(inventoryService, times(1)).reserveStock(orderId, productId1, 2);
        verify(inventoryService, times(1)).reserveStock(orderId, productId2, 1);
        verify(eventPublisher, times(1)).publishInventoryReserved(any(InventoryReservedEvent.class));
        verify(eventPublisher, never()).publishInventoryReservationFailed(any());
        verify(inventoryService, never()).releaseStock(any());
    }

    @Test
    void shouldFailAndReleaseStockWhenInsufficientStock() {
        // Given
        OrderCreatedEvent.OrderItem item1 = new OrderCreatedEvent.OrderItem(
                productId1, "Product 1", 2, BigDecimal.valueOf(50.00)
        );
        OrderCreatedEvent.OrderItem item2 = new OrderCreatedEvent.OrderItem(
                productId2, "Product 2", 1, BigDecimal.valueOf(30.00)
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                customerId,
                BigDecimal.valueOf(130.00),
                List.of(item1, item2),
                LocalDateTime.now()
        );

        when(inventoryService.reserveStock(orderId, productId1, 2)).thenReturn(true);
        when(inventoryService.reserveStock(orderId, productId2, 1)).thenReturn(false); // Second reservation fails

        // When
        sagaHandler.handleOrderCreated(event);

        // Then
        verify(inventoryService, times(1)).reserveStock(orderId, productId1, 2);
        verify(inventoryService, times(1)).reserveStock(orderId, productId2, 1);
        verify(inventoryService, times(1)).releaseStock(orderId); // Compensation
        verify(eventPublisher, never()).publishInventoryReserved(any());
        verify(eventPublisher, times(1)).publishInventoryReservationFailed(any(InventoryReservationFailedEvent.class));
    }

    @Test
    void shouldReleaseStockOnPaymentFailure() {
        // Given
        PaymentFailedEvent event = new PaymentFailedEvent(
                orderId,
                customerId,
                "Payment gateway timeout",
                LocalDateTime.now()
        );

        // When
        sagaHandler.handlePaymentFailed(event);

        // Then
        verify(inventoryService, times(1)).releaseStock(orderId);
    }
}
