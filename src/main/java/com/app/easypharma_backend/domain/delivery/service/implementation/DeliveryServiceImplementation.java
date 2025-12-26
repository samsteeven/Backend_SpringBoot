package com.app.easypharma_backend.domain.delivery.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.delivery.entity.DeliveryStatus;
import com.app.easypharma_backend.domain.delivery.repository.DeliveryRepository;
import com.app.easypharma_backend.domain.delivery.service.interfaces.DeliveryServiceInterface;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImplementation implements DeliveryServiceInterface {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

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
                .build(); // Timestamp handled by @PrePersist

        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery getDeliveryByOrderId(@NonNull UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No delivery found for order ID: " + orderId));
    }
}
