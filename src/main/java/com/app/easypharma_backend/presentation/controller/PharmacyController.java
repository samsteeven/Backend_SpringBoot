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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pharmacies")
@RequiredArgsConstructor
@Tag(name = "Pharmacies", description = "Gestion des pharmacies (CRUD, recherche, statut, géolocalisation)")
public class PharmacyController {

        private final PharmacyServiceInterface pharmacyService;
        private final com.app.easypharma_backend.infrastructure.security.JwtService jwtService;
        private final com.app.easypharma_backend.application.auth.usecase.GetUserProfileUseCase getUserProfileUseCase;
        private final com.app.easypharma_backend.infrastructure.storage.FileStorageService fileStorageService;

        @Operation(summary = "Lister toutes les pharmacies", description = "Retourne la liste complète des pharmacies avec leurs informations.")
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(schema = @Schema(implementation = PharmacyDTO.class)))
        @GetMapping
        public ResponseEntity<List<PharmacyDTO>> getAllPharmacies() {
                return ResponseEntity.ok(pharmacyService.getAllPharmacies());
        }

        @Operation(summary = "Lister TOUTES les pharmacies (Admin)", description = "Retourne la liste complète de toutes les pharmacies, y compris PENDING/REJECTED. Rôle SUPER_ADMIN requis.")
        @GetMapping("/admin/all")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<List<PharmacyDTO>> getAllPharmaciesForAdmin() {
                return ResponseEntity.ok(pharmacyService.getAllPharmaciesForAdmin());
        }

        @Operation(summary = "Récupérer une pharmacie par ID", description = "Retourne les détails d’une pharmacie à partir de son identifiant unique.")
        @ApiResponse(responseCode = "200", description = "Pharmacie trouvée")
        @ApiResponse(responseCode = "404", description = "Pharmacie introuvable")
        @GetMapping("/{id}")
        public ResponseEntity<PharmacyDTO> getPharmacyById(
                        @Parameter(description = "ID de la pharmacie", required = true) @PathVariable @NonNull UUID id) {
                return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
        }

        @Operation(summary = "Récupérer une pharmacie par numéro de licence", description = "Recherche d’une pharmacie en utilisant son numéro de licence unique.")
        @GetMapping("/by-license/{licenseNumber}")
        public ResponseEntity<PharmacyDTO> getByLicenseNumber(
                        @Parameter(description = "Numéro de licence de la pharmacie", required = true) @PathVariable @NonNull String licenseNumber) {
                return ResponseEntity.ok(pharmacyService.getPharmacyByLicenseNumber(licenseNumber));
        }

        @Operation(summary = "Créer une nouvelle pharmacie", description = "Crée un enregistrement de pharmacie en attente de validation. Nécessite le rôle PHARMACIST.")
        @ApiResponse(responseCode = "201", description = "Pharmacie créée")
        @PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
        @PreAuthorize("hasRole('PHARMACY_ADMIN')")
        public ResponseEntity<PharmacyDTO> createPharmacy(
                        @Parameter(description = "Jeton d'authentification Bearer", required = true) @RequestHeader("Authorization") String authHeader,
                        @Parameter(description = "Données de la pharmacie", required = true) @RequestPart("pharmacy") @Valid PharmacyDTO pharmacyDTO,
                        @Parameter(description = "Document de licence (PDF/Image)", required = true) @RequestPart("licenseDocument") org.springframework.web.multipart.MultipartFile licenseDocument) {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);
                var userProfile = getUserProfileUseCase.execute(email);

                pharmacyDTO.setUserId(userProfile.getId());

                // Store the license document
                String licenseUrl = fileStorageService.storeFile(licenseDocument, "documents");
                pharmacyDTO.setLicenseDocumentUrl(licenseUrl);

                PharmacyDTO created = pharmacyService.createPharmacy(pharmacyDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @Operation(summary = "Mettre à jour une pharmacie", description = "Met à jour les informations d’une pharmacie existante. Nécessite le rôle PHARMACIST ou ADMIN.")
        @PutMapping("/{id}")
        @PatchMapping("/{id}")
        @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<PharmacyDTO> updatePharmacy(
                        @PathVariable @NonNull UUID id,
                        @RequestBody @NonNull PharmacyDTO pharmacyDTO) {
                return ResponseEntity.ok(pharmacyService.updatePharmacy(id, pharmacyDTO));
        }

        @Operation(summary = "Changer le statut d’une pharmacie", description = "Change le statut (PENDING, APPROVED, REJECTED, SUSPENDED) d’une pharmacie. Nécessite le rôle ADMIN.")
        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<PharmacyDTO> changeStatus(
                        @PathVariable @NonNull UUID id,
                        @RequestParam @NonNull PharmacyStatus status) {
                return ResponseEntity.ok(pharmacyService.changeStatus(id, status));
        }

        @Operation(summary = "Rechercher par ville", description = "Retourne la liste des pharmacies situées dans une ville donnée.")
        @GetMapping("/search/by-city")
        public ResponseEntity<List<PharmacyDTO>> findByCity(
                        @RequestParam @NonNull String city) {
                return ResponseEntity.ok(pharmacyService.findByCity(city));
        }

        @Operation(summary = "Rechercher par statut", description = "Retourne la liste des pharmacies pour un statut donné.")
        @GetMapping("/search/by-status")
        public ResponseEntity<List<PharmacyDTO>> findByStatus(
                        @RequestParam @NonNull PharmacyStatus status) {
                return ResponseEntity.ok(pharmacyService.findByStatus(status));
        }

        @Operation(summary = "Pharmacies approuvées par ville", description = "Retourne les pharmacies avec statut APPROVED dans une ville donnée.")
        @GetMapping("/approved/by-city")
        public ResponseEntity<List<PharmacyDTO>> findApprovedByCity(
                        @RequestParam @NonNull String city) {
                return ResponseEntity.ok(pharmacyService.findApprovedByCity(city));
        }

        @Operation(summary = "Pharmacies à proximité", description = "Recherche les pharmacies approuvées dans un rayon donné (km) à partir d’une latitude/longitude.")
        @GetMapping("/nearby")
        public ResponseEntity<List<PharmacyDTO>> findNearby(
                        @RequestParam @NonNull Double latitude,
                        @RequestParam @NonNull Double longitude,
                        @RequestParam @NonNull Double radiusKm) {
                return ResponseEntity.ok(
                                pharmacyService.findNearbyPharmacies(latitude, longitude, radiusKm));
        }

        @Operation(summary = "Recherche par nom", description = "Recherche des pharmacies par nom (recherche partielle, insensible à la casse).")
        @GetMapping("/search/by-name")
        public ResponseEntity<List<PharmacyDTO>> searchByName(
                        @RequestParam @NonNull String name) {
                return ResponseEntity.ok(pharmacyService.searchByName(name));
        }

        @Operation(summary = "Supprimer une pharmacie", description = "Supprime une pharmacie par son identifiant. Nécessite le rôle ADMIN.")
        @ApiResponse(responseCode = "204", description = "Pharmacie supprimée")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<Void> deletePharmacy(@PathVariable @NonNull UUID id) {
                pharmacyService.deletePharmacy(id);
                return ResponseEntity.noContent().build();
        }
}