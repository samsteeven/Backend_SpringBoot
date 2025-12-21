package com.app.easypharma_backend.domain.pharmacy.dto;

import com.app.easypharma_backend.domain.auth.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployeeRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "employee@example.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, message = "Le mot de passe doit contenir au moins 4 caractères")
    @Schema(description = "Mot de passe", example = "Password123!")
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    @Schema(description = "Prénom", example = "Jean")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    @Schema(description = "Nom", example = "Dupont")
    private String lastName;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(description = "Téléphone", example = "+237600000000")
    private String phone;

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Rôle (PHARMACY_EMPLOYEE ou DELIVERY)")
    private UserRole role;

    @Schema(description = "Adresse", example = "Douala, Akwa")
    private String address;

    @Schema(description = "Ville", example = "Douala")
    private String city;
}
