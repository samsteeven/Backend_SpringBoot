package com.app.easypharma_backend.domain.delivery.mapper;

import com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir Delivery en DeliveryDTO
 */
@Component
public class DeliveryMapper {

    public DeliveryDTO toDTO(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        return DeliveryDTO.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .orderNumber(delivery.getOrder().getOrderNumber())
                .deliveryPersonId(delivery.getDeliveryPerson().getId())
                .deliveryPersonName(delivery.getDeliveryPerson().getFirstName() + " " + 
                                   delivery.getDeliveryPerson().getLastName())
                .status(delivery.getStatus().toString())
                .currentLatitude(delivery.getCurrentLatitude())
                .currentLongitude(delivery.getCurrentLongitude())
                .photoProofUrl(delivery.getPhotoProofUrl())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .deliveryAddress(delivery.getOrder().getDeliveryAddress())
                .deliveryCity(delivery.getOrder().getDeliveryCity())
                .deliveryPhone(delivery.getOrder().getDeliveryPhone())
                .patientName(delivery.getOrder().getPatient().getFirstName() + " " + 
                           delivery.getOrder().getPatient().getLastName())
                .build();
    }
}
