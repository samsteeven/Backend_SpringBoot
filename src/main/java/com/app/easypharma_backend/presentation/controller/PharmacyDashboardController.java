package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDashboardDTO;
import com.app.easypharma_backend.domain.pharmacy.service.PharmacyDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacyId}/dashboard")
@RequiredArgsConstructor
@Tag(name = "Pharmacy Dashboard", description = "Statistiques et graphiques pour le tableau de bord pharmacie")
public class PharmacyDashboardController {

    private final PharmacyDashboardService pharmacyDashboardService;

    @Operation(summary = "Statistiques du Dashboard", description = "Récupère les chiffres clés et les données des graphiques")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<PharmacyDashboardDTO> getDashboardStats(@PathVariable UUID pharmacyId) {
        // Note: Access control is handled by Spring Security + @PreAuthorize
        // In a real scenario, we'd also check if the user belongs to the pharmacy
        return ResponseEntity.ok(pharmacyDashboardService.getDashboardStats(pharmacyId));
    }
}
