package com.app.easypharma_backend.domain.order.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;
import com.app.easypharma_backend.domain.medication.repository.PharmacyMedicationRepository;
import com.app.easypharma_backend.domain.order.dto.CreateOrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.mapper.OrderMapper;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImplementation implements OrderServiceInterface {

    private final OrderRepository orderRepository;

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PharmacyMedicationRepository pharmacyMedicationRepository;

    private final OrderMapper orderMapper;

    @Override
    public OrderDTO createOrder(@NonNull UUID patientId, @NonNull CreateOrderDTO createOrderDTO) {
        Objects.requireNonNull(patientId, "Patient ID cannot be null");
        Objects.requireNonNull(createOrderDTO, "CreateOrderDTO cannot be null");

        // 1. Validate Patient
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // 2. Validate Pharmacy
        Pharmacy pharmacy = pharmacyRepository
                .findById(Objects.requireNonNull(createOrderDTO.getPharmacyId(), "Pharmacy ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        // 3. Create Order Entity
        Order order = Order.builder()
                .patient(patient)
                .pharmacy(pharmacy)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .deliveryAddress(createOrderDTO.getDeliveryAddress())
                .deliveryCity(createOrderDTO.getDeliveryCity())
                .deliveryPhone(createOrderDTO.getDeliveryPhone())
                .deliveryLatitude(createOrderDTO.getDeliveryLatitude() != null
                        ? BigDecimal.valueOf(createOrderDTO.getDeliveryLatitude())
                        : null)
                .deliveryLongitude(createOrderDTO.getDeliveryLongitude() != null
                        ? BigDecimal.valueOf(createOrderDTO.getDeliveryLongitude())
                        : null)
                .notes(createOrderDTO.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // 4. Validate Stock & Create Items
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderDTO.CreateOrderItemDTO itemDTO : createOrderDTO.getItems()) {
            PharmacyMedication pharmacyMedication = pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(
                    pharmacy.getId(), Objects.requireNonNull(itemDTO.getMedicationId(), "Medication ID cannot be null"))
                    .orElseThrow(() -> new RuntimeException("Medication not found in this pharmacy"));

            if (!pharmacyMedication.getIsAvailable() || pharmacyMedication.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for medication: " + pharmacyMedication.getMedication().getName());
            }

            BigDecimal lineTotal = pharmacyMedication.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order) // linked in helper or save
                    .medication(pharmacyMedication.getMedication())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(pharmacyMedication.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);

        // 5. Save Order (Cascade saves items)
        Order savedOrder = orderRepository.save(order);

        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(@NonNull UUID orderId) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        return orderRepository.findById(orderId)
                .map(orderMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<OrderDTO> getPatientOrders(@NonNull UUID patientId) {
        Objects.requireNonNull(patientId, "Patient ID cannot be null");
        return orderMapper.toDTOList(orderRepository.findByPatientIdOrderByCreatedAtDesc(patientId));
    }

    @Override
    public List<OrderDTO> getPharmacyOrders(@NonNull UUID pharmacyId) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        return orderMapper.toDTOList(orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId));
    }

    @Override
    public OrderDTO updateOrderStatus(@NonNull UUID orderId, @NonNull OrderStatus status) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(status, "Order status cannot be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Handle transitions if needed (e.g., reduce stock on CONFIRMED if not done)
        // For now, simple state update
        order.setStatus(status);
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else if (status == OrderStatus.CONFIRMED || status == OrderStatus.PAID) {
            order.setConfirmedAt(LocalDateTime.now());
            // Reduce stock here if strategy is "Reserve on Confirm"
            // But simpler to assume stock is checked on Create and reduced on Pay/Confirm.
            // Im implementing simple update here.
            // Ideally we should have a separate method "confirmOrder" which handles stock
            // deduction.
            // I'll add stock deduction on PAID/CONFIRMED transition
            if (order.getConfirmedAt() == null) { // First confirmation
                deductStock(order);
            }
        }

        return orderMapper.toDTO(orderRepository.save(order));
    }

    private void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            PharmacyMedication pm = pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(
                    order.getPharmacy().getId(), item.getMedication().getId())
                    .orElseThrow();

            int newStock = pm.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException(
                        "Stock error: Insufficient stock during confirmation for " + pm.getMedication().getName());
            }
            pm.setStockQuantity(newStock);
            if (newStock == 0)
                pm.setIsAvailable(false);
            pharmacyMedicationRepository.save(pm);
        }
    }

    @Override
    public void validateStockAvailability(@NonNull UUID pharmacyId,
            @NonNull List<CreateOrderDTO.CreateOrderItemDTO> items) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(items, "Items list cannot be null");

        for (CreateOrderDTO.CreateOrderItemDTO itemDTO : items) {
            PharmacyMedication pharmacyMedication = pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(
                    pharmacyId, itemDTO.getMedicationId())
                    .orElseThrow(() -> new RuntimeException("Medication not available"));

            if (pharmacyMedication.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + pharmacyMedication.getMedication().getName());
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
