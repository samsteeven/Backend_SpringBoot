package com.app.easypharma_backend.application.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String oldPassword;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 4, message = "Le nouveau mot de passe doit contenir au moins 4 caractères")
    private String newPassword;
}