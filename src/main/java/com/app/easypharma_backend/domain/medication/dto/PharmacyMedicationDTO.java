package com.app.easypharma_backend.domain.medication.dto;

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
public class PharmacyMedicationDTO {
    private UUID id;
    private UUID pharmacyId;
    private MedicationDTO medication;
    private UUID medicationId; // Useful for creation when we just pass the ID
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isAvailable;
}
