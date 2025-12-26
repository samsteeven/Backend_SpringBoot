package com.app.easypharma_backend.domain.delivery.service.interfaces;

import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface DeliveryServiceInterface {

    /**
     * Assigne une commande à un livreur
     *
     * @param pharmacyId ID de la pharmacie (pour vérification)
     * @param orderId    ID de la commande
     * @param courierId  ID du livreur
     * @return La livraison créée
     */
    Delivery assignDelivery(@NonNull UUID pharmacyId, @NonNull UUID orderId, @NonNull UUID courierId);

    /**
     * Récupère la livraison associée à une commande
     * 
     * @param orderId ID de la commande
     * @return La livraison ou une exception si non trouvée
     */
    Delivery getDeliveryByOrderId(@NonNull UUID orderId);
}
