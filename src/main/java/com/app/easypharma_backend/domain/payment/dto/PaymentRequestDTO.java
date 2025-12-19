package com.app.easypharma_backend.domain.payment.dto;

import com.app.easypharma_backend.domain.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private UUID orderId;
    private PaymentMethod method; // MTN_MOMO, ORANGE_MONEY
    private String phoneNumber;
}
