package com.app.easypharma_backend.application.auth.dto.response;

import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}