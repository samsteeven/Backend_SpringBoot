package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.service.interfaces.PharmacyServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacies")
@RequiredArgsConstructor
@Tag(name = "Pharmacies", description = "Gestion des pharmacies (CRUD, recherche, statut, géolocalisation)")
public class PharmacyController {

    private final PharmacyServiceInterface pharmacyService;

    @Operation(
            summary = "Lister toutes les pharmacies",
            description = "Retourne la liste complète des pharmacies avec leurs informations."
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = PharmacyDTO.class)))
    @GetMapping
    public ResponseEntity<List<PharmacyDTO>> getAllPharmacies() {
        return ResponseEntity.ok(pharmacyService.getAllPharmacies());
    }

    @Operation(
            summary = "Récupérer une pharmacie par ID",
            description = "Retourne les détails d’une pharmacie à partir de son identifiant unique."
    )
    @ApiResponse(responseCode = "200", description = "Pharmacie trouvée")
    @ApiResponse(responseCode = "404", description = "Pharmacie introuvable")
    @GetMapping("/{id}")
    public ResponseEntity<PharmacyDTO> getPharmacyById(
            @Parameter(description = "ID de la pharmacie", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    @Operation(
            summary = "Récupérer une pharmacie par numéro de licence",
            description = "Recherche d’une pharmacie en utilisant son numéro de licence unique."
    )
    @GetMapping("/by-license/{licenseNumber}")
    public ResponseEntity<PharmacyDTO> getByLicenseNumber(
            @Parameter(description = "Numéro de licence de la pharmacie", required = true)
            @PathVariable String licenseNumber) {
        return ResponseEntity.ok(pharmacyService.getPharmacyByLicenseNumber(licenseNumber));
    }

    @Operation(
            summary = "Créer une nouvelle pharmacie",
            description = "Crée un enregistrement de pharmacie en attente de validation."
    )
    @ApiResponse(responseCode = "201", description = "Pharmacie créée")
    @PostMapping
    public ResponseEntity<PharmacyDTO> createPharmacy(
            @RequestBody PharmacyDTO pharmacyDTO) {
        PharmacyDTO created = pharmacyService.createPharmacy(pharmacyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Mettre à jour une pharmacie",
            description = "Met à jour les informations d’une pharmacie existante."
    )
    @PutMapping("/{id}")
    public ResponseEntity<PharmacyDTO> updatePharmacy(
            @PathVariable UUID id,
            @RequestBody PharmacyDTO pharmacyDTO) {
        return ResponseEntity.ok(pharmacyService.updatePharmacy(id, pharmacyDTO));
    }

    @Operation(
            summary = "Changer le statut d’une pharmacie",
            description = "Change le statut (PENDING, APPROVED, REJECTED, SUSPENDED) d’une pharmacie."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<PharmacyDTO> changeStatus(
            @PathVariable UUID id,
            @RequestParam PharmacyStatus status) {
        return ResponseEntity.ok(pharmacyService.changeStatus(id, status));
    }

    @Operation(
            summary = "Rechercher par ville",
            description = "Retourne la liste des pharmacies situées dans une ville donnée."
    )
    @GetMapping("/search/by-city")
    public ResponseEntity<List<PharmacyDTO>> findByCity(
            @RequestParam String city) {
        return ResponseEntity.ok(pharmacyService.findByCity(city));
    }

    @Operation(
            summary = "Rechercher par statut",
            description = "Retourne la liste des pharmacies pour un statut donné."
    )
    @GetMapping("/search/by-status")
    public ResponseEntity<List<PharmacyDTO>> findByStatus(
            @RequestParam PharmacyStatus status) {
        return ResponseEntity.ok(pharmacyService.findByStatus(status));
    }

    @Operation(
            summary = "Pharmacies approuvées par ville",
            description = "Retourne les pharmacies avec statut APPROVED dans une ville donnée."
    )
    @GetMapping("/approved/by-city")
    public ResponseEntity<List<PharmacyDTO>> findApprovedByCity(
            @RequestParam String city) {
        return ResponseEntity.ok(pharmacyService.findApprovedByCity(city));
    }

    @Operation(
            summary = "Pharmacies à proximité",
            description = "Recherche les pharmacies approuvées dans un rayon donné (km) à partir d’une latitude/longitude."
    )
    @GetMapping("/nearby")
    public ResponseEntity<List<PharmacyDTO>> findNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusKm) {
        return ResponseEntity.ok(
                pharmacyService.findNearbyPharmacies(latitude, longitude, radiusKm)
        );
    }

    @Operation(
            summary = "Recherche par nom",
            description = "Recherche des pharmacies par nom (recherche partielle, insensible à la casse)."
    )
    @GetMapping("/search/by-name")
    public ResponseEntity<List<PharmacyDTO>> searchByName(
            @RequestParam String name) {
        return ResponseEntity.ok(pharmacyService.searchByName(name));
    }

    @Operation(
            summary = "Supprimer une pharmacie",
            description = "Supprime une pharmacie par son identifiant."
    )
    @ApiResponse(responseCode = "204", description = "Pharmacie supprimée")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacy(@PathVariable UUID id) {
        pharmacyService.deletePharmacy(id);
        return ResponseEntity.noContent().build();
    }
}