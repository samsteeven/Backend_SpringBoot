package com.app.easypharma_backend.domain.order.dto;

import com.app.easypharma_backend.domain.order.entity.PrescriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PrescriptionDTO {
    private UUID id;
    private String photoUrl;
    private PrescriptionStatus status;
    private String pharmacistComment;
    private LocalDateTime createdAt;
    private UUID orderId; // Optional link
}
