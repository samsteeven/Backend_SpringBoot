package com.app.easypharma_backend.domain.delivery.service.interfaces;

import com.app.easypharma_backend.domain.delivery.dto.AvailableOrderDTO;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;
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

    /**
     * Récupère les commandes disponibles pour livraison auprès de la pharmacie du livreur
     * 
     * @param deliveryPersonId ID du livreur
     * @param userLatitude Latitude du livreur (optionnel, pour calcul de distance)
     * @param userLongitude Longitude du livreur (optionnel, pour calcul de distance)
     * @return Liste des commandes disponibles à livrer
     */
    List<AvailableOrderDTO> getAvailableOrders(@NonNull UUID deliveryPersonId, 
                                                BigDecimal userLatitude, 
                                                BigDecimal userLongitude);

    /**
     * Le livreur accepte une commande et se voit assigner cette livraison
     * 
     * @param deliveryPersonId ID du livreur
     * @param orderId ID de la commande à accepter
     * @return La livraison créée/assignée
     */
    Delivery acceptOrder(@NonNull UUID deliveryPersonId, @NonNull UUID orderId);
}
