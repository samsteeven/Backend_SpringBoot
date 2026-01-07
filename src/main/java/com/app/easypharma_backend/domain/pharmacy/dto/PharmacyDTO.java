package com.app.easypharma_backend.domain.pharmacy.dto;

import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.lang.NonNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PharmacyDTO {

    private UUID id;

    private UUID userId;

    private String ownerFirstName;

    private String ownerLastName;

    private String ownerEmail;

    private String name;

    private String licenseNumber;

    private String address;

    private String city;

    private String phone;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String description;

    private String openingHours;

    private Double averageRating;

    private Integer ratingCount;

    private PharmacyStatus status;

    private String licenseDocumentUrl;

    private LocalDateTime validatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NonNull
    public UUID getUserId() {
        return userId;
    }
}