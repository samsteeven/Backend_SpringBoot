package com.app.easypharma_backend.domain.delivery.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.delivery.dto.AvailableOrderDTO;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.delivery.entity.DeliveryStatus;
import com.app.easypharma_backend.domain.delivery.repository.DeliveryRepository;
import com.app.easypharma_backend.domain.delivery.service.interfaces.DeliveryServiceInterface;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImplementation implements DeliveryServiceInterface {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public Delivery assignDelivery(@NonNull UUID pharmacyId, @NonNull UUID orderId, @NonNull UUID courierId) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(courierId, "Courier ID cannot be null");

        // 1. Validate Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // 2. Validate Pharmacy Ownership
        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("This order does not belong to the specified pharmacy");
        }

        // 3. Validate Courier
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found with ID: " + courierId));

        if (courier.getRole() != UserRole.DELIVERY) {
            throw new RuntimeException("User is not a courier (Role: " + courier.getRole() + ")");
        }

        // 4. Verify Courier belongs to Pharmacy (Optional, depends on business logic)
        // Assuming couriers are linked to pharmacies via User.pharmacy
        if (courier.getPharmacy() == null || !courier.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("Courier is not employed by this pharmacy");
        }

        // 5. Check if already assigned
        if (deliveryRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Delivery already assigned for this order");
        }

        // 6. Create Delivery
        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPerson(courier)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // Notify Courier
        String courierTitle = "Nouvelle livraison attribuée";
        String courierMsg = String.format("Vous avez une nouvelle livraison à effectuer pour la commande %s.",
                order.getOrderNumber());
        notificationService.sendInAppNotification(courier, courierTitle, courierMsg, NotificationType.DELIVERY);
        notificationService.sendSms(courier.getPhone(), courierMsg);

        // Notify Patient
        String patientTitle = "Livreur attribué";
        String patientMsg = String.format("Votre commande %s a été confiée à %s %s pour la livraison.",
                order.getOrderNumber(), courier.getFirstName(), courier.getLastName());
        notificationService.sendInAppNotification(order.getPatient(), patientTitle, patientMsg,
                NotificationType.DELIVERY);
        notificationService.sendPushNotification(order.getPatient(), patientTitle, patientMsg);

        return saved;
    }

    @Override
    public Delivery getDeliveryByOrderId(@NonNull UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No delivery found for order ID: " + orderId));
    }

    @Override
    public List<AvailableOrderDTO> getAvailableOrders(@NonNull UUID deliveryPersonId, 
                                                       BigDecimal userLatitude, 
                                                       BigDecimal userLongitude) {
        Objects.requireNonNull(deliveryPersonId, "Delivery person ID cannot be null");

        // 1. Vérifier que le livreur existe et a le rôle DELIVERY
        User deliveryPerson = userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        if (deliveryPerson.getRole() != UserRole.DELIVERY) {
            throw new RuntimeException("L'utilisateur n'est pas un livreur");
        }

        // 2. Récupérer la pharmacie du livreur
        if (deliveryPerson.getPharmacy() == null) {
            throw new RuntimeException("Le livreur n'est assigné à aucune pharmacie");
        }

        UUID pharmacyId = deliveryPerson.getPharmacy().getId();

        // 3. Récupérer toutes les commandes READY de cette pharmacie qui n'ont pas de livraison
        List<Order> readyOrders = orderRepository.findByPharmacyIdAndStatus(pharmacyId, OrderStatus.READY);

        // 4. Filtrer les commandes qui n'ont pas déjà une livraison assignée
        List<Order> availableOrders = readyOrders.stream()
                .filter(order -> !deliveryRepository.existsByOrderId(order.getId()))
                .collect(Collectors.toList());

        // 5. Convertir en DTOs avec calcul de distance si position fournie
        return availableOrders.stream()
                .map(order -> buildAvailableOrderDTO(order, userLatitude, userLongitude))
                .collect(Collectors.toList());
    }

    @Override
    public Delivery acceptOrder(@NonNull UUID deliveryPersonId, @NonNull UUID orderId) {
        Objects.requireNonNull(deliveryPersonId, "Delivery person ID cannot be null");
        Objects.requireNonNull(orderId, "Order ID cannot be null");

        // 1. Vérifier que le livreur existe et a le rôle DELIVERY
        User deliveryPerson = userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        if (deliveryPerson.getRole() != UserRole.DELIVERY) {
            throw new RuntimeException("L'utilisateur n'est pas un livreur");
        }

        // 2. Vérifier que le livreur a une pharmacie
        if (deliveryPerson.getPharmacy() == null) {
            throw new RuntimeException("Le livreur n'est assigné à aucune pharmacie");
        }

        UUID pharmacyId = deliveryPerson.getPharmacy().getId();

        // 3. Vérifier que la commande existe et appartient à la pharmacie du livreur
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("La commande n'appartient pas à votre pharmacie");
        }

        // 4. Vérifier que la commande est en statut READY (prête pour livraison)
        if (order.getStatus() != OrderStatus.READY) {
            throw new RuntimeException("La commande n'est pas prête pour la livraison. Statut actuel: " + order.getStatus());
        }

        // 5. Vérifier qu'une livraison n'existe pas déjà
        if (deliveryRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Une livraison est déjà assignée à cette commande");
        }

        // 6. Créer la livraison
        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPerson(deliveryPerson)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // 7. Notifier le patient que la commande est assignée à un livreur
        String patientTitle = "Livreur assigné à votre commande";
        String patientMsg = String.format("Votre commande %s a été assignée à %s %s pour la livraison.",
                order.getOrderNumber(), deliveryPerson.getFirstName(), deliveryPerson.getLastName());
        notificationService.sendInAppNotification(order.getPatient(), patientTitle, patientMsg,
                NotificationType.DELIVERY);
        notificationService.sendPushNotification(order.getPatient(), patientTitle, patientMsg);

        return saved;
    }

    /**
     * Construit un DTO à partir d'une commande avec calcul de distance optionnel
     */
    private AvailableOrderDTO buildAvailableOrderDTO(Order order, BigDecimal userLatitude, BigDecimal userLongitude) {
        AvailableOrderDTO dto = AvailableOrderDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .patientName(order.getPatient().getFirstName() + " " + order.getPatient().getLastName())
                .patientPhone(order.getPatient().getPhone())
                .patientEmail(order.getPatient().getEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryCity(order.getDeliveryCity())
                .deliveryPhone(order.getDeliveryPhone())
                .deliveryLatitude(order.getDeliveryLatitude())
                .deliveryLongitude(order.getDeliveryLongitude())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedAt())
                .pharmacyId(order.getPharmacy().getId())
                .pharmacyName(order.getPharmacy().getName())
                .pharmacyLatitude(order.getPharmacy().getLatitude())
                .pharmacyLongitude(order.getPharmacy().getLongitude())
                .build();

        // Calculer la distance si les coordonnées du livreur sont fournies
        if (userLatitude != null && userLongitude != null && 
            order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null) {
            double distance = calculateDistance(
                    userLatitude.doubleValue(),
                    userLongitude.doubleValue(),
                    order.getDeliveryLatitude().doubleValue(),
                    order.getDeliveryLongitude().doubleValue()
            );
            dto.setDistanceKm(distance);
        }

        return dto;
    }

    /**
     * Calcule la distance en km entre deux points GPS (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
}
