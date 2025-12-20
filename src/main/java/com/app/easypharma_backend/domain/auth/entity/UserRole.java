package com.app.easypharma_backend.domain.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rôles disponibles pour les utilisateurs")
public enum UserRole {
    @Schema(description = "Administrateur système - Gère tous les utilisateurs")
    SUPER_ADMIN,

    @Schema(description = "Propriétaire de pharmacie - Peut créer et gérer sa pharmacie")
    PHARMACY_ADMIN,

    @Schema(description = "Employé pharmacien - Traite les commandes et affecte les livreurs")
    PHARMACY_EMPLOYEE,

    @Schema(description = "Client/Patient - Peut commander des médicaments")
    PATIENT,

    @Schema(description = "Livreur - Livre les commandes")
    DELIVERY
}