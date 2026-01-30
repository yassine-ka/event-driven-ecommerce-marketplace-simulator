package com.example.order.domain;

/**
 * Order status enum representing the lifecycle of an order in the Saga pattern.
 * 
 * PENDING -> Order created, waiting for inventory reservation
 * PROCESSING -> Inventory reserved, payment processing
 * COMPLETED -> Payment successful, order fulfilled
 * CANCELLED -> Order cancelled due to failure or user action
 * FAILED -> Order failed (inventory or payment failure)
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    FAILED
}
