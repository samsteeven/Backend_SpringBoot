package com.app.easypharma_backend.domain.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDataDTO {
    private String date; // Format YYYY-MM-DD or Month Name
    private BigDecimal amount;
}
