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
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import com.app.easypharma_backend.domain.order.mapper.OrderMapper;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.infrastructure.validation.StatusTransitionValidator;
import com.app.easypharma_backend.domain.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderServiceImplementation implements OrderServiceInterface {

    private final OrderRepository orderRepository;

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PharmacyMedicationRepository pharmacyMedicationRepository;

    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final StatusTransitionValidator statusValidator;
    private final AuditLogService auditLogService;
    private final com.app.easypharma_backend.infrastructure.pdf.PdfService pdfService;
    private final com.app.easypharma_backend.domain.delivery.service.interfaces.DeliveryServiceInterface deliveryService;

    @Override
    public byte[] generateInvoicePdf(@NonNull UUID orderId) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        return pdfService.generateInvoice(order);
    }

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

        // CRITICAL: Ensure pharmacy is APPROVED before allowing orders
        if (pharmacy.getStatus() != com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus.APPROVED) {
            throw new RuntimeException(
                    "Cannot create order: Pharmacy is not approved. Status: " + pharmacy.getStatus());
        }

        // Calculate Delivery Fee
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (createOrderDTO.getDeliveryLatitude() != null && createOrderDTO.getDeliveryLongitude() != null &&
            pharmacy.getLatitude() != null && pharmacy.getLongitude() != null) {
            
            double distance = calculateDistance(
                pharmacy.getLatitude().doubleValue(), pharmacy.getLongitude().doubleValue(),
                createOrderDTO.getDeliveryLatitude(), createOrderDTO.getDeliveryLongitude()
            );
            deliveryFee = deliveryService.calculateDeliveryFee(distance);
        } else {
             // Default if no coordinates (should not happen if address validation exists)
             // Or throw exception? Let's assume default fee.
             deliveryFee = deliveryService.calculateDeliveryFee(5.0); // Assume 5km default
        }

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
                .deliveryFee(deliveryFee)
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

        // Add fee to total or keep separate? Usually Total Amount includes everything.
        // Let's keep totalAmount as merchandise total and maybe add a grandTotal logic or include fee.
        // For simplicity, let's say totalAmount = items + fee.
        totalAmount = totalAmount.add(deliveryFee);
        order.setTotalAmount(totalAmount);

        // 5. Save Order (Cascade saves items)
        Order savedOrder = orderRepository.save(order);

        // Notify Pharmacist
        String pharmacistTitle = "Nouvelle commande reçue";
        String pharmacistMsg = String.format("Vous avez une nouvelle commande (%s) à préparer.",
                savedOrder.getOrderNumber());
        notificationService.sendInAppNotification(pharmacy.getUser(), pharmacistTitle, pharmacistMsg,
                NotificationType.ORDER);
        notificationService.sendEmail(pharmacy.getUser().getEmail(), pharmacistTitle, pharmacistMsg);

        return orderMapper.toDTO(savedOrder);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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

        // VALIDATION: Vérifier que la transition de statut est autorisée
        OrderStatus oldStatus = order.getStatus();
        statusValidator.validateTransition(oldStatus, status);
        log.info("Status transition validated: {} -> {} for order {}", oldStatus, status, orderId);

        // Handle transitions if needed (e.g., reduce stock on CONFIRMED if not done)
        // For now, simple state update
        // APRÈS : Ajouter la gestion de l'annulation
        // Si on annule une commande qui avait déjà déduit le stock (donc
        // confirmée/payée)
        if (status == OrderStatus.CANCELLED
                && (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PAID)) {
            restockItems(order);
        }

        order.setStatus(status);

        // AUDIT: Logger le changement de statut
        try {
            java.util.Map<String, Object> auditDetails = java.util.Map.of(
                    "orderId", orderId.toString(),
                    "orderNumber", order.getOrderNumber(),
                    "oldStatus", oldStatus.toString(),
                    "newStatus", status.toString(),
                    "pharmacyId", order.getPharmacy().getId().toString());
            auditLogService.logAction(
                    UUID.randomUUID(), // TODO: Get from Authentication
                    "SYSTEM", // TODO: Get from Authentication
                    "UPDATE_ORDER_STATUS",
                    "ORDER",
                    orderId,
                    auditDetails,
                    "INTERNAL",
                    "OrderService",
                    order.getPharmacy().getId());
        } catch (Exception e) {
            log.error("Failed to log audit for order status update", e);
        }

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else if (status == OrderStatus.CONFIRMED || status == OrderStatus.PAID) {
            // Check if this is the first confirmation BEFORE setting confirmedAt
            boolean isFirstConfirmation = (order.getConfirmedAt() == null);

            order.setConfirmedAt(LocalDateTime.now());
            // Reduce stock here if strategy is "Reserve on Confirm"
            if (isFirstConfirmation) { // First confirmation
                deductStock(order);

                // Notify Patient
                String patientTitle = "Commande confirmée";
                String patientMsg = String.format(
                        "Votre commande %s a été confirmée par la pharmacie et est en cours de préparation.",
                        order.getOrderNumber());
                notificationService.sendInAppNotification(order.getPatient(), patientTitle, patientMsg,
                        NotificationType.ORDER);
                notificationService.sendSms(order.getPatient().getPhone(), patientMsg);
                notificationService.sendEmail(order.getPatient().getEmail(), patientTitle, patientMsg);
            }
        } else if (status == OrderStatus.READY) {
            // Trigger Auto-Assignment
            try {
                deliveryService.autoAssignDelivery(order);
            } catch (Exception e) {
                // Log warning but don't block state change? Or block?
                // User requirement: "Algorithme d'assignation".
                // If assignment fails, maybe we should warn user or keep it in READY but not assigned?
                // For now, log error and notify pharmacy?
                log.error("Failed to auto-assign delivery for order {}", order.getId(), e);
                // We could send a notification to Pharmacy Admin saying "No courier found"
            }
        }

        return orderMapper.toDTO(orderRepository.save(order));
    }

    private void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            PharmacyMedication pm = pharmacyMedicationRepository.findByPharmacyIdAndMedicationIdForUpdate(
                    order.getPharmacy().getId(), item.getMedication().getId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable au moment de la validation"));

            int newStock = pm.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Stock insuffisant pour : " + pm.getMedication().getName());
            }
            pm.setStockQuantity(newStock);
            pm.setIsAvailable(newStock > 0);
            pharmacyMedicationRepository.save(pm);
        }
    }

    // AJOUTER cette méthode pour gérer les annulations
    private void restockItems(Order order) {
        for (OrderItem item : order.getItems()) {
            PharmacyMedication pm = pharmacyMedicationRepository.findByPharmacyIdAndMedicationIdForUpdate(
                    order.getPharmacy().getId(), item.getMedication().getId())
                    .orElse(null);
            if (pm != null) {
                pm.setStockQuantity(pm.getStockQuantity() + item.getQuantity());
                pm.setIsAvailable(true);
                pharmacyMedicationRepository.save(pm);
            }
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

    @Override
    public java.math.BigDecimal getPharmacyRevenue(@NonNull UUID pharmacyId) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        java.math.BigDecimal revenue = orderRepository.calculateTotalRevenueByPharmacy(pharmacyId);
        return revenue != null ? revenue : java.math.BigDecimal.ZERO;
    }
}
