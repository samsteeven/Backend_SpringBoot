package com.app.easypharma_backend.domain.order.entity;

public enum OrderStatus {
    PENDING,
    PAID,
    CONFIRMED,
    PREPARING,
    READY,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED
}