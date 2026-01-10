package com.app.easypharma_backend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour les commandes disponibles à livrer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableOrderDTO {
    private UUID orderId;
    private String orderNumber;
    
    // Informations patient
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    
    // Adresse de livraison
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPhone;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    
    // Informations commande
    private BigDecimal totalAmount;
    private String status; // PENDING, PAID, CONFIRMED, READY
    private LocalDateTime createdAt;
    
    // Distance (calculée si position du livreur fournie)
    private Double distanceKm;
    
    // Informations pharmacie
    private UUID pharmacyId;
    private String pharmacyName;
    private BigDecimal pharmacyLatitude;
    private BigDecimal pharmacyLongitude;
}
