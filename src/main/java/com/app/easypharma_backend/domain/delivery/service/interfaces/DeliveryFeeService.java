package com.app.easypharma_backend.domain.delivery.service.interfaces;

import java.math.BigDecimal;

public interface DeliveryFeeService {
    /**
     * Calcule les frais de livraison basés sur la distance
     * 
     * @param distanceKm Distance en kilomètres
     * @return Montant des frais de livraison
     */
    BigDecimal calculateFee(double distanceKm);
}
