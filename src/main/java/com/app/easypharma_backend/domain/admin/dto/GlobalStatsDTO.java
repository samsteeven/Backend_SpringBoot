package com.app.easypharma_backend.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsDTO {
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal totalRevenue;
    private long activePharmacies;
    private long totalPatients;
}
