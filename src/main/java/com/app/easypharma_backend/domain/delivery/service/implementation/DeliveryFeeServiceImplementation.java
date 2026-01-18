package com.app.easypharma_backend.domain.delivery.service.implementation;

import com.app.easypharma_backend.domain.delivery.service.interfaces.DeliveryFeeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DeliveryFeeServiceImplementation implements DeliveryFeeService {

    // On pourrait mettre ces valeurs dans application.properties plus tard
    private static final BigDecimal BASE_FEE = new BigDecimal("500.00");
    private static final BigDecimal RATE_PER_KM = new BigDecimal("100.00");

    @Override
    public BigDecimal calculateFee(double distanceKm) {
        if (distanceKm <= 0) {
            return BigDecimal.ZERO;
        }

        // Formule : Base + (Distance * Taux)
        // On arrondit à l'unité supérieure ou on garde 2 décimales
        BigDecimal distanceRate = RATE_PER_KM.multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal total = BASE_FEE.add(distanceRate);

        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
