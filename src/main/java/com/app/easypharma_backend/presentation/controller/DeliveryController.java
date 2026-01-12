package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.delivery.entity.DeliveryStatus;
import com.app.easypharma_backend.domain.delivery.repository.DeliveryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "Gestion des livraisons pour les livreurs")
public class DeliveryController {

    private final DeliveryRepository deliveryRepository;
    private final com.app.easypharma_backend.domain.delivery.service.interfaces.DeliveryServiceInterface deliveryService;
    private final com.app.easypharma_backend.domain.auth.repository.UserRepository userRepository;
    private final com.app.easypharma_backend.domain.delivery.mapper.DeliveryMapper deliveryMapper;

    @Operation(summary = "Assigner une livraison", description = "Le pharmacien assigne une commande à un livreur de sa pharmacie")
    @PostMapping("/assign")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<ApiResponse<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>> assignDelivery(
            @Parameter(description = "ID de la commande") @RequestParam UUID orderId,
            @Parameter(description = "ID du livreur") @RequestParam UUID courierId,
            Authentication authentication) {

        var pharmacist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        if (pharmacist.getPharmacy() == null) {
            throw new RuntimeException("Pharmacist is not associated with any pharmacy");
        }

        Delivery delivery = deliveryService.assignDelivery(pharmacist.getPharmacy().getId(), orderId, courierId);
        return ResponseEntity.ok(ApiResponse.success(deliveryMapper.toDTO(delivery), "Livraison assignée avec succès"));
    }

    @Operation(summary = "Mes livraisons", description = "Récupère toutes les livraisons assignées au livreur connecté")
    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<List<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>>> getMyDeliveries(
            Authentication authentication) {
        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        List<Delivery> deliveries = deliveryRepository.findByDeliveryPersonIdOrderByCreatedAtDesc(deliveryPersonId);
        return ResponseEntity.ok(ApiResponse.success(deliveries.stream().map(deliveryMapper::toDTO).toList(),
                "Livraisons récupérées avec succès"));
    }

    @Operation(summary = "Livraisons en cours", description = "Récupère uniquement les livraisons en cours (ASSIGNED, PICKED_UP, IN_TRANSIT)")
    @GetMapping("/my-deliveries/ongoing")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<List<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>>> getOngoingDeliveries(
            Authentication authentication) {
        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        List<Delivery> deliveries = deliveryRepository.findOngoingDeliveriesByPerson(deliveryPersonId);
        return ResponseEntity.ok(ApiResponse.success(deliveries.stream().map(deliveryMapper::toDTO).toList(),
                "Livraisons en cours récupérées"));
    }

    @Operation(summary = "Mettre à jour le statut", description = "Met à jour le statut d'une livraison")
    @PatchMapping("/{deliveryId}/status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>> updateDeliveryStatus(
            @Parameter(description = "ID de la livraison") @PathVariable UUID deliveryId,
            @Parameter(description = "Nouveau statut") @RequestParam DeliveryStatus status,
            Authentication authentication) {

        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        // Vérifier que la livraison appartient bien au livreur connecté
        if (!delivery.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette livraison");
        }

        delivery.setStatus(status);

        // Mettre à jour les timestamps selon le statut
        switch (status) {
            case PICKED_UP:
                delivery.setPickedUpAt(LocalDateTime.now());
                break;
            case DELIVERED:
                delivery.setDeliveredAt(LocalDateTime.now());
                break;
            default:
                break;
        }

        Delivery updated = deliveryRepository.save(delivery);
        return ResponseEntity.ok(ApiResponse.success(deliveryMapper.toDTO(updated), "Statut mis à jour"));
    }

    @Operation(summary = "Mettre à jour la position", description = "Met à jour la position GPS actuelle du livreur")
    @PatchMapping("/{deliveryId}/location")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>> updateLocation(
            @Parameter(description = "ID de la livraison") @PathVariable UUID deliveryId,
            @Parameter(description = "Latitude") @RequestParam BigDecimal latitude,
            @Parameter(description = "Longitude") @RequestParam BigDecimal longitude,
            Authentication authentication) {

        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        if (!delivery.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette livraison");
        }

        delivery.setCurrentLatitude(latitude);
        delivery.setCurrentLongitude(longitude);

        Delivery updated = deliveryRepository.save(delivery);
        return ResponseEntity.ok(ApiResponse.success(deliveryMapper.toDTO(updated), "Position mise à jour"));
    }

    @Operation(summary = "Ajouter une preuve de livraison", description = "Ajoute l'URL de la photo de preuve de livraison")
    @PatchMapping("/{deliveryId}/proof")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<com.app.easypharma_backend.domain.delivery.dto.DeliveryDTO>> addProofPhoto(
            @Parameter(description = "ID de la livraison") @PathVariable UUID deliveryId,
            @Parameter(description = "URL de la photo") @RequestParam String photoUrl,
            Authentication authentication) {

        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        if (!delivery.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette livraison");
        }

        delivery.setPhotoProofUrl(photoUrl);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        Delivery updated = deliveryRepository.save(delivery);
        return ResponseEntity.ok(ApiResponse.success(deliveryMapper.toDTO(updated), "Preuve de livraison ajoutée"));
    }

    @Operation(summary = "Statistiques du livreur", description = "Récupère les statistiques de livraison du livreur")
    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryStats>> getMyStats(Authentication authentication) {
        UUID deliveryPersonId = getUserIdFromAuthentication(authentication);

        Long completed = deliveryRepository.countCompletedDeliveriesByPerson(deliveryPersonId);
        Long failed = deliveryRepository.countFailedDeliveriesByPerson(deliveryPersonId);
        List<Delivery> ongoing = deliveryRepository.findOngoingDeliveriesByPerson(deliveryPersonId);

        DeliveryStats stats = DeliveryStats.builder()
                .completedDeliveries(completed)
                .failedDeliveries(failed)
                .ongoingDeliveries((long) ongoing.size())
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats, "Statistiques récupérées"));
    }

    /**
     * Méthode utilitaire pour extraire l'ID utilisateur depuis l'authentification
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }

    /**
     * DTO pour les statistiques du livreur
     */
    @lombok.Data
    @lombok.Builder
    public static class DeliveryStats {
        private Long completedDeliveries;
        private Long failedDeliveries;
        private Long ongoingDeliveries;
    }
}
