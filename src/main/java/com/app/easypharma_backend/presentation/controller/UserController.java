package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.ChangeUserRoleRequest;
import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.request.ChangePasswordRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.usecase.*;
import com.app.easypharma_backend.application.common.dto.PageResponse;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Imports OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
@Tag(name = "Users", description = "API pour gérer les utilisateurs (profil, administration)")
public class UserController {

        private final GetUserProfileUseCase getUserProfileUseCase;
        private final UpdateUserProfileUseCase updateUserProfileUseCase;
        private final GetUserByIdUseCase getUserByIdUseCase;
        private final DeleteUserUseCase deleteUserUseCase;
        private final ListUsersUseCase listUsersUseCase;
        private final UpdateUserByIdUseCase updateUserByIdUseCase;
        private final DeleteUserByIdUseCase deleteUserByIdUseCase;
        private final ChangeUserRoleUseCase changeUserRoleUseCase;
        private final ChangeUserPasswordUseCase changeUserPasswordUseCase;
        private final com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository pharmacyRepository;
        private final JwtService jwtService;

        /**
         * Récupère le profil de l'utilisateur connecté
         */
        @Operation(summary = "Récupère le profil de l'utilisateur connecté", description = "Permet à un utilisateur authentifié de récupérer ses propres informations de profil", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @GetMapping("/me")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> getCurrentUser(
                        @Parameter(description = "Jeton d'authentification Bearer", required = true) @RequestHeader("Authorization") String authHeader) {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);

                UserResponse response = getUserProfileUseCase.execute(email);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Profil utilisateur récupéré"));
        }

        /**
         * Met à jour le profil de l'utilisateur connecté
         */
        @Operation(summary = "Met à jour le profil de l'utilisateur connecté", description = "Permet à un utilisateur authentifié de mettre à jour ses propres informations de profil", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil utilisateur mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide - Données de mise à jour incorrectes"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @PutMapping("/me")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> updateCurrentUser(
                        @Parameter(description = "Jeton d'authentification Bearer", required = true) @RequestHeader("Authorization") String authHeader,
                        @Parameter(description = "Données de mise à jour de l'utilisateur", required = true) @Valid @RequestBody UpdateUserRequest request) {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);

                UserResponse response = updateUserProfileUseCase.execute(email, request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Profil utilisateur mis à jour"));
        }

        /**
         * Récupère un utilisateur par son ID
         */
        @Operation(summary = "Récupère un utilisateur par son ID", description = "Permet à un utilisateur authentifié de récupérer les informations d'un autre utilisateur par son identifiant", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur récupéré avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès interdit - Permissions insuffisantes"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @GetMapping("/{id}")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> getUserById(
                        @Parameter(description = "Identifiant unique de l'utilisateur", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") @PathVariable UUID id) {
                UserResponse response = getUserByIdUseCase.execute(id);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Utilisateur récupéré"));
        }

        /**
         * Supprime le compte de l'utilisateur connecté
         */
        @Operation(summary = "Supprime le compte de l'utilisateur connecté", description = "Permet à un utilisateur authentifié de supprimer définitivement son propre compte", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Compte utilisateur supprimé avec succès"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant")
        })
        @DeleteMapping("/me")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> deleteCurrentUser(
                        @Parameter(description = "Jeton d'authentification Bearer", required = true) @RequestHeader("Authorization") String authHeader) {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);

                deleteUserUseCase.execute(email);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Compte utilisateur supprimé"));
        }

        /**
         * Change le mot de passe de l'utilisateur connecté
         */
        @Operation(summary = "Change le mot de passe de l'utilisateur connecté", description = "Permet à un utilisateur authentifié de modifier son mot de passe", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe modifié avec succès"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide - Ancien mot de passe incorrect ou nouveau mot de passe invalide"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant")
        })
        @PatchMapping("/me/password")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> changePassword(
                        @Parameter(description = "Jeton d'authentification Bearer", required = true) @RequestHeader("Authorization") String authHeader,
                        @Parameter(description = "Données de changement de mot de passe", required = true) @Valid @RequestBody ChangePasswordRequest request) {
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);

                changeUserPasswordUseCase.execute(email, request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Mot de passe modifié avec succès"));
        }

        // ... existing constructor implicitly handled by @RequiredArgsConstructor ...

        /**
         * Liste tous les utilisateurs (réservé aux administrateurs)
         * Peut être filtré par rôle
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Liste paginée des utilisateurs (ADMIN)", description = "Permet à un administrateur de récupérer la liste paginée de tous les utilisateurs, avec filtre optionnel par rôle", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès interdit - Permissions administrateur requises")
        })
        @GetMapping
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<PageResponse<UserResponse>>> getAllUsers(
                        @Parameter(description = "Numéro de page (commence à 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Taille de la page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Filtrer par rôle (optionnel)", required = false) @RequestParam(required = false) com.app.easypharma_backend.domain.auth.entity.UserRole role) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<UserResponse> response = listUsersUseCase.execute(pageable, role, null);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Liste des utilisateurs"));
        }

        /**
         * Liste les employés de ma pharmacie (réservé aux PHARMACY_ADMIN)
         */
        @PreAuthorize("hasRole('PHARMACY_ADMIN')")
        @Operation(summary = "Liste les employés de ma pharmacie", description = "Permet à un administrateur de pharmacie de voir ses employés", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/my-pharmacy")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<PageResponse<UserResponse>>> getMyPharmacyUsers(
                        @Parameter(description = "Numéro de page", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Taille de la page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Jeton d'authentification", required = true) @RequestHeader("Authorization") String authHeader) {

                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);
                // On récupère l'ID de l'utilisateur connecté via un usecase existant ou repo
                // (ici getUserProfileUseCase pour faire simple et sûr)
                UserResponse currentUser = getUserProfileUseCase.execute(email);
                UUID userId = currentUser.getId();

                // Trouver la pharmacie du user (On suppose qu'il en a une car PHARMACY_ADMIN)
                var pharmacy = pharmacyRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Aucune pharmacie associée à ce compte administrateur"));

                Pageable pageable = PageRequest.of(page, size);
                // On filtre par pharmacyId, et on met role à null pour avoir tous les types
                // d'employés (employee + delivery)
                // Ou on pourrait vouloir filtrer. Pour l'instant on liste tout le monde de la
                // pharmacie.
                PageResponse<UserResponse> response = listUsersUseCase.execute(pageable, null, pharmacy.getId());

                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Liste des employés de la pharmacie"));
        }

        /**
         * Met à jour un utilisateur par ID (réservé aux administrateurs)
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Met à jour un utilisateur par ID (ADMIN)", description = "Permet à un administrateur de mettre à jour les informations d'un utilisateur spécifique par son identifiant", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide - Données de mise à jour incorrectes"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès interdit - Permissions administrateur requises"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @PutMapping("/{id}")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> updateUserById(
                        @Parameter(description = "Identifiant unique de l'utilisateur", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") @PathVariable UUID id,
                        @Parameter(description = "Données de mise à jour de l'utilisateur", required = true) @Valid @RequestBody UpdateUserRequest request) {
                UserResponse response = updateUserByIdUseCase.execute(id, request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Utilisateur mis à jour"));
        }

        /**
         * Supprime un utilisateur par ID (réservé aux administrateurs)
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Supprime un utilisateur par ID (ADMIN)", description = "Permet à un administrateur de supprimer définitivement un utilisateur spécifique par son identifiant", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur supprimé avec succès"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès interdit - Permissions administrateur requises"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> deleteUserById(
                        @Parameter(description = "Identifiant unique de l'utilisateur", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") @PathVariable UUID id) {
                deleteUserByIdUseCase.execute(id);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Utilisateur supprimé"));
        }

        /**
         * Change le rôle d'un utilisateur (réservé aux administrateurs)
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Change le rôle d'un utilisateur (ADMIN)", description = "Permet à un administrateur de modifier le rôle d'un utilisateur spécifique (ADMIN, PHARMACIST, CUSTOMER)", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rôle utilisateur modifié avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide - Rôle spécifié incorrect"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé - Jeton invalide ou manquant"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès interdit - Permissions administrateur requises"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
        })
        @PatchMapping("/{id}/role")
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> changeUserRole(
                        @Parameter(description = "Identifiant unique de l'utilisateur", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") @PathVariable UUID id,
                        @Parameter(description = "Nouveau rôle à attribuer à l'utilisateur", required = true) @Valid @RequestBody ChangeUserRoleRequest request) {
                UserResponse response = changeUserRoleUseCase.execute(id, request.getRole());
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Rôle utilisateur modifié"));
        }
}