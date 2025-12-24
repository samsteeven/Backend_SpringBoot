package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.medication.dto.PatientMedicationSearchResultDTO;
import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;
import com.app.easypharma_backend.domain.medication.repository.PharmacyMedicationRepository;
import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.mapper.PharmacyMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patient/search")
@RequiredArgsConstructor
@Tag(name = "Patient Search", description = "Recherche intelligente pour les patients (Médicaments, Symptômes, Tri)")
public class PatientSearchController {

    private final PharmacyMedicationRepository pharmacyMedicationRepository;
    private final PharmacyMapper pharmacyMapper;
    private final com.app.easypharma_backend.infrastructure.security.JwtService jwtService;
    private final com.app.easypharma_backend.application.auth.usecase.GetUserProfileUseCase getUserProfileUseCase;

    @Operation(summary = "Recherche globale de médicaments", description = "Recherche par nom, générique ou symptômes. Tri par prix, note ou proximité. Si 'query' est vide, retourne tous les médicaments disponibles.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Résultats de la recherche", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatientMedicationSearchResultDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Paramètres invalides (ex: tri NEAREST sans coordonnées)", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping
    public ResponseEntity<List<PatientMedicationSearchResultDTO>> searchMedications(
            @Parameter(description = "Terme de recherche (optionnel)") @RequestParam(defaultValue = "") String query,
            @Parameter(description = "Classe thérapeutique") @RequestParam(required = false) com.app.easypharma_backend.domain.medication.entity.TherapeuticClass therapeuticClass,
            @Parameter(description = "Critère de tri: PRICE, RATING, NEAREST") @RequestParam(defaultValue = "PRICE") String sortBy,
            @Parameter(description = "Latitude utilisateur (requis pour tri NEAREST)") @RequestParam(required = false) Double userLat,
            @Parameter(description = "Longitude utilisateur (requis pour tri NEAREST)") @RequestParam(required = false) Double userLon,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Auto-detect location for authenticated users if not provided
        if ((userLat == null || userLon == null) && authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);
                var userProfile = getUserProfileUseCase.execute(email);

                if (userLat == null && userProfile.getLatitude() != null) {
                    userLat = userProfile.getLatitude().doubleValue();
                }
                if (userLon == null && userProfile.getLongitude() != null) {
                    userLon = userProfile.getLongitude().doubleValue();
                }
            } catch (Exception e) {
                // Ignore invalid token or missing user profile
            }
        }

        List<PharmacyMedication> results;
        if ("POSTGIS_NEAREST".equalsIgnoreCase(sortBy)) {
            if (userLat == null || userLon == null) {
                return ResponseEntity.badRequest().build();
            }
            // Use PostGIS native query for efficient spatial sorting
            String therapeuticClassStr = therapeuticClass != null ? therapeuticClass.name() : null;
            results = pharmacyMedicationRepository.searchGlobalPostGIS(query, therapeuticClassStr, userLat, userLon);
        } else {
            // Default JPQL search
            results = pharmacyMedicationRepository.searchGlobal(query, therapeuticClass);
        }

        Double finalUserLat = userLat;
        Double finalUserLon = userLon;
        List<PatientMedicationSearchResultDTO> dtos = results.stream()
                .map(pm -> {
                    PharmacyDTO pharmacyDTO = pharmacyMapper.toDTO(pm.getPharmacy());

                    Double distance = null;
                    if (finalUserLat != null && finalUserLon != null && pm.getPharmacy().getLatitude() != null
                            && pm.getPharmacy().getLongitude() != null) {
                        distance = calculateDistance(finalUserLat, finalUserLon,
                                pm.getPharmacy().getLatitude().doubleValue(),
                                pm.getPharmacy().getLongitude().doubleValue());
                    }

                    return PatientMedicationSearchResultDTO.builder()
                            .medicationId(pm.getMedication().getId())
                            .medicationName(pm.getMedication().getName())
                            .medicationGenericName(pm.getMedication().getGenericName())
                            .medicationDescription(pm.getMedication().getDescription())
                            .medicationPhotoUrl(pm.getMedication().getPhotoUrl())
                            .price(pm.getPrice())
                            .isAvailable(pm.getIsAvailable())
                            .pharmacy(pharmacyDTO)
                            .distanceKm(distance)
                            .build();
                })
                .collect(Collectors.toList());

        // Sorting logic (java-side) only if NOT using PostGIS
        if (!"POSTGIS_NEAREST".equalsIgnoreCase(sortBy)) {
            switch (sortBy.toUpperCase()) {
                case "RATING":
                    dtos.sort(Comparator.comparing(
                            dto -> dto.getPharmacy().getAverageRating() != null ? dto.getPharmacy().getAverageRating()
                                    : 0.0,
                            Comparator.reverseOrder()));
                    break;
                case "NEAREST":
                    if (userLat != null && userLon != null) {
                        dtos.sort(Comparator
                                .comparing(
                                        dto -> dto.getDistanceKm() != null ? dto.getDistanceKm() : Double.MAX_VALUE));
                    }
                    break;
                case "PRICE":
                default:
                    dtos.sort(Comparator.comparing(PatientMedicationSearchResultDTO::getPrice));
                    break;
            }
        }

        return ResponseEntity.ok(dtos);
    }

    // Haversine formula for distance in km
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
