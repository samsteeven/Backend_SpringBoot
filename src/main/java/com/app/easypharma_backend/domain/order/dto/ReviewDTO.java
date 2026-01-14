package com.app.easypharma_backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private String pharmacyName;
    private String courierName;
    private Integer pharmacyRating;
    private String pharmacyComment;
    private Integer courierRating;
    private String courierComment;
    private String status;
    private LocalDateTime createdAt;
}
