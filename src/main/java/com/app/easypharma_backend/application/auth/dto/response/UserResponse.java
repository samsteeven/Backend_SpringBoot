package com.app.easypharma_backend.application.auth.dto.response;

import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Identifiant unique de l'utilisateur", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID id;
    
    @Schema(description = "Adresse email de l'utilisateur", example = "utilisateur@example.com")
    private String email;
    
    @Schema(description = "Prénom de l'utilisateur", example = "Jean")
    private String firstName;
    
    @Schema(description = "Nom de famille de l'utilisateur", example = "Dupont")
    private String lastName;
    
    @Schema(description = "Numéro de téléphone de l'utilisateur", example = "+33123456789")
    private String phone;
    
    @Schema(description = "Rôle de l'utilisateur dans le système")
    private UserRole role;
    
    @Schema(description = "Adresse de l'utilisateur", example = "123 Rue de la Paix")
    private String address;
    
    @Schema(description = "Ville de l'utilisateur", example = "Paris")
    private String city;
    
    @Schema(description = "Latitude de la position géographique de l'utilisateur", example = "48.8566")
    private BigDecimal latitude;
    
    @Schema(description = "Longitude de la position géographique de l'utilisateur", example = "2.3522")
    private BigDecimal longitude;
    
    @Schema(description = "Statut d'activation du compte utilisateur")
    private Boolean isActive;
    
    @Schema(description = "Statut de vérification de l'adresse email")
    private Boolean isVerified;
    
    @Schema(description = "Date de création du compte utilisateur")
    private LocalDateTime createdAt;
}