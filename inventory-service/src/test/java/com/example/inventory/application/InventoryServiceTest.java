package com.example.inventory.application;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockReservation;
import com.example.inventory.infrastructure.repository.ProductRepository;
import com.example.inventory.infrastructure.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        product = Product.builder()
                .id(productId)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(10)
                .version(0L)
                .build();
    }

    @Test
    void shouldReserveStockSuccessfully() {
        // Given
        int quantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean result = inventoryService.reserveStock(orderId, productId, quantity);

        // Then
        assertThat(result).isTrue();
        assertThat(product.getStockQuantity()).isEqualTo(5); // 10 - 5 = 5
        verify(productRepository, times(1)).save(product);
        verify(reservationRepository, times(1)).save(any(StockReservation.class));
    }

    @Test
    void shouldFailReservationWhenInsufficientStock() {
        // Given
        int quantity = 15; // More than available (10)
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        boolean result = inventoryService.reserveStock(orderId, productId, quantity);

        // Then
        assertThat(result).isFalse();
        assertThat(product.getStockQuantity()).isEqualTo(10); // Unchanged
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(StockReservation.class));
    }

    @Test
    void shouldFailReservationWhenProductNotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.reserveStock(orderId, nonExistentProductId, 5);
        });
    }

    @Test
    void shouldReleaseStockSuccessfully() {
        // Given
        StockReservation reservation1 = StockReservation.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .product(product)
                .quantity(3)
                .status(StockReservation.ReservationStatus.RESERVED)
                .build();

        StockReservation reservation2 = StockReservation.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .product(product)
                .quantity(2)
                .status(StockReservation.ReservationStatus.RESERVED)
                .build();

        product.setStockQuantity(5); // After initial reservation

        when(reservationRepository.findByOrderIdAndStatus(
                orderId, StockReservation.ReservationStatus.RESERVED))
                .thenReturn(Arrays.asList(reservation1, reservation2));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        inventoryService.releaseStock(orderId);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(10); // 5 + 3 + 2 = 10
        assertThat(reservation1.getStatus()).isEqualTo(StockReservation.ReservationStatus.RELEASED);
        assertThat(reservation2.getStatus()).isEqualTo(StockReservation.ReservationStatus.RELEASED);
        verify(productRepository, times(2)).save(product);
        verify(reservationRepository, times(2)).save(any(StockReservation.class));
    }

    @Test
    void shouldGetAllProducts() {
        // Given
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .sku("TEST-002")
                .name("Test Product 2")
                .price(BigDecimal.valueOf(75.00))
                .stockQuantity(20)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        // When
        List<Product> result = inventoryService.getAllProducts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(product, product2);
    }

    @Test
    void shouldGetProductById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Product result = inventoryService.getProduct(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.getProduct(nonExistentProductId);
        });
    }
}
