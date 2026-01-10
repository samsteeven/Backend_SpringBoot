package com.app.easypharma_backend.domain.medication.dto;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicationSearchResultDTO {
    private UUID medicationId;
    private String medicationName;
    private String medicationGenericName;
    private String medicationDescription;
    private String medicationPhotoUrl;

    // Price & Stock
    private BigDecimal price;
    private Boolean isAvailable;
    private Integer stockQuantity; // Add stock quantity for client display

    // Pharmacy Details
    private PharmacyDTO pharmacy;

    // Contextual Info (calculated)
    private Double distanceKm; // Filled if user location provided
}
