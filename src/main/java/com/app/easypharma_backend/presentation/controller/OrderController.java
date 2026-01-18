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
    private final UserRepository userRepository;
    private final com.app.easypharma_backend.domain.employee.service.EmployeePermissionService permissionService;

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
        UUID userId = getCurrentUserId();
        com.app.easypharma_backend.domain.auth.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sécurité : Vérifier que le pharmacien appartient bien à cette pharmacie
        if (user.getPharmacy() == null || !user.getPharmacy().getId().equals(pharmacyId)) {
            throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                    "Accès refusé : Vous n'appartenez pas à cette pharmacie");
        }

        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        return ResponseEntity.ok(orderService.getPharmacyOrders(pharmacyId));
    }

    @Operation(summary = "Revenus de la pharmacie", description = "Calcule le chiffre d'affaires total (livré)")
    @GetMapping("/pharmacy-stats/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<java.math.BigDecimal> getPharmacyStats(@PathVariable @NonNull UUID pharmacyId) {
        UUID userId = getCurrentUserId();
        com.app.easypharma_backend.domain.auth.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sécurité : Sauf pour SUPER_ADMIN, vérifier l'appartenance
        if (!"SUPER_ADMIN".equals(user.getRole().name())) {
            if (user.getPharmacy() == null || !user.getPharmacy().getId().equals(pharmacyId)) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : Vous n'avez pas accès aux stats de cette pharmacie");
            }
        }

        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        return ResponseEntity.ok(orderService.getPharmacyRevenue(pharmacyId));
    }

    @Operation(summary = "Détails commande", description = "Récupère les détails d'une commande")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Patient or Pharmacist
    public ResponseEntity<OrderDTO> getOrder(@PathVariable @NonNull UUID id) {
        Objects.requireNonNull(id, "Order ID cannot be null");
        OrderDTO order = orderService.getOrderById(id);

        // Sécurité supplémentaire : Vérifier si l'utilisateur a le droit de voir cette
        // commande
        UUID userId = getCurrentUserId();
        com.app.easypharma_backend.domain.auth.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("PATIENT".equals(user.getRole().name())) {
            if (!order.getPatientId().equals(userId)) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : Cette commande ne vous appartient pas");
            }
        } else if ("PHARMACY_ADMIN".equals(user.getRole().name())
                || "PHARMACY_EMPLOYEE".equals(user.getRole().name())) {
            if (user.getPharmacy() == null || !order.getPharmacyId().equals(user.getPharmacy().getId())) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : Cette commande appartient à une autre pharmacie");
            }
        }

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Mettre à jour statut", description = "Pharmacien valide ou marque livré")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable @NonNull UUID id,
            @RequestParam @NonNull OrderStatus status,
            Authentication authentication) {
        Objects.requireNonNull(id, "Order ID cannot be null");
        Objects.requireNonNull(status, "Order status cannot be null");

        UUID userId = getCurrentUserId();
        com.app.easypharma_backend.domain.auth.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderDTO order = orderService.getOrderById(id);

        // Sécurité : Vérifier que la commande appartient à la pharmacie de
        // l'utilisateur
        if (!"SUPER_ADMIN".equals(user.getRole().name())) {
            if (user.getPharmacy() == null || !order.getPharmacyId().equals(user.getPharmacy().getId())) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : Vous ne pouvez pas modifier une commande d'une autre pharmacie");
            }
        }

        // Vérifier permission PREPARE_ORDERS pour employés
        if ("PHARMACY_EMPLOYEE".equals(user.getRole().name())) {
            if (status == OrderStatus.PREPARING || status == OrderStatus.READY) {
                if (!permissionService.hasPermission(userId, "PREPARE_ORDERS")) {
                    throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                            "Permission denied: canPrepareOrders required");
                }
            }
        }

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
