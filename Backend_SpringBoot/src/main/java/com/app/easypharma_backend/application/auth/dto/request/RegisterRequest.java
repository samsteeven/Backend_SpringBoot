package com.app.easypharma_backend.application.auth.dto.request;

import com.app.easypharma_backend.domain.auth.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "utilisateur@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Le mot de passe doit contenir au moins une minuscule, une majuscule et un chiffre"
    )
    @Schema(description = "Mot de passe de l'utilisateur (doit contenir au moins une minuscule, une majuscule et un chiffre)", 
            example = "MotDePasse123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    @Schema(description = "Prénom de l'utilisateur", example = "Jean", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Schema(description = "Nom de famille de l'utilisateur", example = "Dupont", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Size(min = 9, max = 9, message = "Le numero de téléphone doit contenir 09 caractères")
    @Schema(description = "Numéro de téléphone de l'utilisateur", example = "+33123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Rôle de l'utilisateur dans le système", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;

    @Schema(description = "Adresse de l'utilisateur", example = "123 Rue de la Paix")
    private String address;

    @Schema(description = "Ville de l'utilisateur", example = "Paris")
    private String city;

    @DecimalMin(value = "-90.0", message = "La latitude doit être entre -90 et 90")
    @DecimalMax(value = "90.0", message = "La latitude doit être entre -90 et 90")
    @Schema(description = "Latitude de la position géographique de l'utilisateur", example = "48.8566")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être entre -180 et 180")
    @DecimalMax(value = "180.0", message = "La longitude doit être entre -180 et 180")
    @Schema(description = "Longitude de la position géographique de l'utilisateur", example = "2.3522")
    private BigDecimal longitude;
}