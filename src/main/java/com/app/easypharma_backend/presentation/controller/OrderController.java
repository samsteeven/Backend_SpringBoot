package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.order.dto.CreateOrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Gestion des commandes")
public class OrderController {

    private final OrderServiceInterface orderService;
    private final UserRepository userRepository; // To fetch ID from Auth

    @Operation(summary = "Créer une commande", description = "Le patient crée une commande")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @NonNull CreateOrderDTO createOrderDTO) {
        UUID patientId = getCurrentUserId();
        return ResponseEntity.ok(orderService.createOrder(patientId, createOrderDTO));
    }

    @Operation(summary = "Mes commandes (Patient)", description = "Récupère les commandes du patient connecté")
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        UUID patientId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getPatientOrders(patientId));
    }

    @Operation(summary = "Commandes de ma pharmacie (Pharmacien)", description = "Récupère les commandes reçues par la pharmacie du pharmacien connecté")
    @GetMapping("/pharmacy-orders/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<List<OrderDTO>> getPharmacyOrders(@PathVariable @NonNull UUID pharmacyId) {
        // En théorie, on devrait vérifier si le pharmacien connecté est bien proprio de
        // cette pharmacie
        // Pour l'instant on laisse passer si Role Pharmacist
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        return ResponseEntity.ok(orderService.getPharmacyOrders(pharmacyId));
    }

    @Operation(summary = "Revenus de la pharmacie", description = "Calcule le chiffre d'affaires total (livré)")
    @GetMapping("/pharmacy-stats/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<java.math.BigDecimal> getPharmacyStats(@PathVariable @NonNull UUID pharmacyId) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        return ResponseEntity.ok(orderService.getPharmacyRevenue(pharmacyId));
    }

    @Operation(summary = "Détails commande", description = "Récupère les détails d'une commande")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Patient or Pharmacist
    public ResponseEntity<OrderDTO> getOrder(@PathVariable @NonNull UUID id) {
        Objects.requireNonNull(id, "Order ID cannot be null");
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Mettre à jour statut", description = "Pharmacien valide ou marque livré")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable @NonNull UUID id,
            @RequestParam @NonNull OrderStatus status) {
        Objects.requireNonNull(id, "Order ID cannot be null");
        Objects.requireNonNull(status, "Order status cannot be null");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @NonNull
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UUID id = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return Objects.requireNonNull(id, "User ID cannot be null");
    }
}
