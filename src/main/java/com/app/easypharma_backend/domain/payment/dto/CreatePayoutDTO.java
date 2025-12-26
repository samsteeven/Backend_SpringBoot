package com.app.easypharma_backend.domain.payment.dto;

import com.app.easypharma_backend.domain.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayoutDTO {
    private UUID pharmacyId;
    private BigDecimal amount;
    private String transactionReference;
    private PaymentMethod method;
    private String notes;
}
