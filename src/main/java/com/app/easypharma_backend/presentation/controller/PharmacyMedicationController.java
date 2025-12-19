package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacyId}/medications")
@RequiredArgsConstructor
@Tag(name = "Pharmacy Inventory", description = "Gestion de l'inventaire des pharmacies")
public class PharmacyMedicationController {

    private final PharmacyMedicationServiceInterface pharmacyMedicationService;

    @Operation(summary = "Lister l'inventaire", description = "Récupère tous les médicaments disponibles dans une pharmacie")
    @GetMapping
    public ResponseEntity<List<PharmacyMedicationDTO>> getInventory(@PathVariable UUID pharmacyId) {
        return ResponseEntity.ok(pharmacyMedicationService.getPharmacyInventory(pharmacyId));
    }

    @Operation(summary = "Ajouter un médicament", description = "Ajoute un médicament à l'inventaire de la pharmacie")
    @PostMapping
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PharmacyMedicationDTO> addMedication(
            @PathVariable UUID pharmacyId,
            @RequestParam UUID medicationId,
            @RequestParam BigDecimal price,
            @RequestParam Integer stock) {
        return ResponseEntity
                .ok(pharmacyMedicationService.addMedicationToPharmacy(pharmacyId, medicationId, price, stock));
    }

    @Operation(summary = "Mettre à jour le stock", description = "Met à jour la quantité en stock d'un médicament")
    @PatchMapping("/{medicationId}/stock")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PharmacyMedicationDTO> updateStock(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicationId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(pharmacyMedicationService.updateStock(pharmacyId, medicationId, quantity));
    }

    @Operation(summary = "Mettre à jour le prix", description = "Met à jour le prix d'un médicament")
    @PatchMapping("/{medicationId}/price")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PharmacyMedicationDTO> updatePrice(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicationId,
            @RequestParam BigDecimal price) {
        return ResponseEntity.ok(pharmacyMedicationService.updatePrice(pharmacyId, medicationId, price));
    }

    @Operation(summary = "Supprimer un médicament", description = "Retire un médicament de l'inventaire")
    @DeleteMapping("/{medicationId}")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<Void> removeMedication(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicationId) {
        pharmacyMedicationService.removeMedicationFromPharmacy(pharmacyId, medicationId);
        return ResponseEntity.noContent().build();
    }
}
