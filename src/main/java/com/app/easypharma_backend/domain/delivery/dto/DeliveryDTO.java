package com.app.easypharma_backend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour représenter une livraison
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {
    private UUID deliveryId;
    private UUID orderId;
    private String orderNumber;
    
    // Livreur assigné
    private UUID deliveryPersonId;
    private String deliveryPersonName;
    
    // Statut
    private String status; // ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED
    
    // Localisation
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private String photoProofUrl;
    
    // Dates
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    
    // Détails de livraison
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPhone;
    private String patientName;
}
