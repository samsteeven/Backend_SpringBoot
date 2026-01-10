package com.app.easypharma_backend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques du livreur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatsDTO {
    private Long completedDeliveries;
    private Long failedDeliveries;
    private Long ongoingDeliveries;
    private Double averageDeliveryTimeMinutes;
    private Double successRate; // pourcentage
}
