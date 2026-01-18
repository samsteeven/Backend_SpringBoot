package com.app.easypharma_backend.domain.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDashboardDTO {
    private long totalOrders;
    private long pendingOrders;
    private long activeDeliveries;
    private BigDecimal monthlyRevenue;
    private int lowStockCount;
    private int expiringSoonCount;

    private List<RevenueDataDTO> revenueEvolution;
    private List<TopProductDTO> topProducts;
    private Map<String, Long> orderStatusDistribution;
}
