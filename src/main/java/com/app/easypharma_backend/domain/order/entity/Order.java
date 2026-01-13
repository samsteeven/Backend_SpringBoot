package com.app.easypharma_backend.domain.order.entity;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String deliveryCity;

    @Column(name = "delivery_phone", nullable = false, length = 20)
    private String deliveryPhone;

    @Column(name = "delivery_latitude", precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;

    @Column(name = "delivery_longitude", precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method pour ajouter un item
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}