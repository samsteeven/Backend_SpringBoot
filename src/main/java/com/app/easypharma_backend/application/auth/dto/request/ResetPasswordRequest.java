package com.app.easypharma_backend.application.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Le token est obligatoire")
    @Schema(description = "Jeton de réinitialisation du mot de passe reçu par email", 
            example = "dGhpcyBpcyBhIHJlc2V0IHBhc3N3b3JkIHRva2VuIHJlY2VpdmVkIGJ5IGVtYWls", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Schema(description = "Nouveau mot de passe de l'utilisateur", 
            example = "NouveauMotDePasse123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}