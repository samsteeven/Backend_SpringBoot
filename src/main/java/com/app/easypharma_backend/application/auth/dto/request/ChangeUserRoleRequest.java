package com.app.easypharma_backend.application.auth.dto.request;

import com.app.easypharma_backend.domain.auth.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private UserRole role;
}