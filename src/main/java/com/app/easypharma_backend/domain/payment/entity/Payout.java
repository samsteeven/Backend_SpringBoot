package com.app.easypharma_backend.domain.payment.entity;

import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payouts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String transactionReference; // e.g., "MOMO-123456"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // MTN_MOMO_PAYOUT, CASH_PAYOUT

    private String notes;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
