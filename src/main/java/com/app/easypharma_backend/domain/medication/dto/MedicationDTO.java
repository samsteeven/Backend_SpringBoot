package com.app.easypharma_backend.domain.medication.dto;

import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {
    private UUID id;
    private String name;
    private String genericName;
    private TherapeuticClass therapeuticClass;
    private String description;
    private String dosage;
    private String photoUrl;
    private String noticePdfUrl;
    private Boolean requiresPrescription;
}
