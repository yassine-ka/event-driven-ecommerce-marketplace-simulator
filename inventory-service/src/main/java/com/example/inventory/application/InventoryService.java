package com.example.inventory.application;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockReservation;
import com.example.inventory.infrastructure.repository.ProductRepository;
import com.example.inventory.infrastructure.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for inventory operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;

    /**
     * Reserves stock for an order.
     * Returns true if successful, false if insufficient stock.
     */
    @Transactional
    public boolean reserveStock(UUID orderId, UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Check stock availability
        if (product.getStockQuantity() < quantity) {
            log.warn("Insufficient stock for product {}: requested {}, available {}", 
                    productId, quantity, product.getStockQuantity());
            return false;
        }

        // Reserve stock (optimistic locking via @Version)
        product.setStockQuantity(product.getStockQuantity() - quantity);
        try {
            productRepository.save(product);
        } catch (Exception e) {
            log.error("Failed to reserve stock for product {}: {}", productId, e.getMessage());
            return false;
        }

        // Create reservation record
        StockReservation reservation = StockReservation.builder()
                .orderId(orderId)
                .product(product)
                .quantity(quantity)
                .status(StockReservation.ReservationStatus.RESERVED)
                .build();
        reservationRepository.save(reservation);

        log.info("Reserved {} units of product {} for order {}", quantity, productId, orderId);
        return true;
    }

    /**
     * Releases reserved stock (compensation action).
     */
    @Transactional
    public void releaseStock(UUID orderId) {
        List<StockReservation> reservations = reservationRepository
                .findByOrderIdAndStatus(orderId, StockReservation.ReservationStatus.RESERVED);

        for (StockReservation reservation : reservations) {
            Product product = reservation.getProduct();
            product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
            productRepository.save(product);

            reservation.setStatus(StockReservation.ReservationStatus.RELEASED);
            reservation.setReleasedAt(java.time.LocalDateTime.now());
            reservationRepository.save(reservation);

            log.info("Released {} units of product {} for order {}", 
                    reservation.getQuantity(), product.getId(), orderId);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }
}
