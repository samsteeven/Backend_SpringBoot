package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacyId}/medications")
@RequiredArgsConstructor
@Tag(name = "Pharmacy Inventory", description = "Gestion de l'inventaire des pharmacies")
public class PharmacyMedicationController {

    private final PharmacyMedicationServiceInterface pharmacyMedicationService;
    private final UserRepository userRepository;

    private void validatePharmacyAccess(UUID pharmacyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                    "Utilisateur non authentifié");
        }

        String email = auth.getName();
        com.app.easypharma_backend.domain.auth.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"SUPER_ADMIN".equals(user.getRole().name())) {
            if (user.getPharmacy() == null || !user.getPharmacy().getId().equals(pharmacyId)) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : Vous ne pouvez pas gérer l'inventaire de cette pharmacie");
            }
        }
    }

    @Operation(summary = "Lister l'inventaire", description = "Récupère tous les médicaments disponibles dans une pharmacie")
    @GetMapping
    public ResponseEntity<List<PharmacyMedicationDTO>> getInventory(@PathVariable @NonNull UUID pharmacyId) {
        return ResponseEntity.ok(pharmacyMedicationService.getPharmacyInventory(pharmacyId));
    }

    @Operation(summary = "Ajouter un médicament", description = "Ajoute un médicament à l'inventaire de la pharmacie")
    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<PharmacyMedicationDTO> addMedication(
            @PathVariable @NonNull UUID pharmacyId,
            @RequestParam @NonNull UUID medicationId,
            @RequestParam @NonNull BigDecimal price,
            @RequestParam @NonNull Integer stock,
            @RequestParam(required = false) LocalDate expiryDate) {
        validatePharmacyAccess(pharmacyId);
        return ResponseEntity
                .ok(pharmacyMedicationService.addMedicationToPharmacy(pharmacyId, medicationId, price, stock,
                        expiryDate));
    }

    @Operation(summary = "Mettre à jour le stock", description = "Met à jour la quantité en stock d'un médicament")
    @PatchMapping("/{medicationId}/stock")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<PharmacyMedicationDTO> updateStock(
            @PathVariable @NonNull UUID pharmacyId,
            @PathVariable @NonNull UUID medicationId,
            @RequestParam @NonNull Integer quantity) {
        validatePharmacyAccess(pharmacyId);
        return ResponseEntity.ok(pharmacyMedicationService.updateStock(pharmacyId, medicationId, quantity));
    }

    @Operation(summary = "Mettre à jour le prix", description = "Met à jour le prix d'un médicament")
    @PatchMapping("/{medicationId}/price")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<PharmacyMedicationDTO> updatePrice(
            @PathVariable @NonNull UUID pharmacyId,
            @PathVariable @NonNull UUID medicationId,
            @RequestParam @NonNull BigDecimal price) {
        validatePharmacyAccess(pharmacyId);
        return ResponseEntity.ok(pharmacyMedicationService.updatePrice(pharmacyId, medicationId, price));
    }

    @Operation(summary = "Supprimer un médicament", description = "Retire un médicament de l'inventaire")
    @DeleteMapping("/{medicationId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<Void> removeMedication(
            @PathVariable @NonNull UUID pharmacyId,
            @PathVariable @NonNull UUID medicationId) {
        validatePharmacyAccess(pharmacyId);
        pharmacyMedicationService.removeMedicationFromPharmacy(pharmacyId, medicationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importer l'inventaire", description = "Importation en masse via CSV")
    @PostMapping("/import")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<Integer> importMedications(
            @PathVariable @NonNull UUID pharmacyId,
            @RequestBody String csvContent) {
        validatePharmacyAccess(pharmacyId);
        return ResponseEntity.ok(pharmacyMedicationService.importMedications(pharmacyId, csvContent));
    }
}
