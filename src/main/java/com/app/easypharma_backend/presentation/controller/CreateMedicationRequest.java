package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicationRequest {
    private String name;
    private String genericName;
    private TherapeuticClass therapeuticClass;
    private String description;
    private String dosage;
    private Boolean requiresPrescription;
}
