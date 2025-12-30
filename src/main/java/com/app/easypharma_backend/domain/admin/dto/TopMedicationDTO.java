package com.app.easypharma_backend.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopMedicationDTO {
    private String name;
    private long count; // Can be sold count or search count
    private String type; // "SOLD" or "SEARCHED"
}
