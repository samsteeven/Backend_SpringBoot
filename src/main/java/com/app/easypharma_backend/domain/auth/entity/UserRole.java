package com.app.easypharma_backend.domain.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rôles disponibles pour les utilisateurs")
public enum UserRole {
    @Schema(description = "Administrateur du système")
    ADMIN,
    
    @Schema(description = "Pharmacien")
    PHARMACIST,
    
    @Schema(description = "Client/Utilisateur standard")
    CUSTOMER,

    @Schema(description = "Livreur")
    DELIVERY
}