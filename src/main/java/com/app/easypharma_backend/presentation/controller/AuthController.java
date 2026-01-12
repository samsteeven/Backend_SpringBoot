package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.*;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.usecase.*;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
@Tag(name = "Authentification", description = "Endpoints d'authentification et de gestion des utilisateurs")
public class AuthController {

        private final RegisterUseCase registerUseCase;
        private final LoginUseCase loginUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;
        private final LogoutUseCase logoutUseCase;
        private final ForgotPasswordUseCase forgotPasswordUseCase;
        private final ResetPasswordUseCase resetPasswordUseCase;
        private final GetUserProfileUseCase getUserProfileUseCase;
        private final JwtService jwtService;
        private final com.app.easypharma_backend.infrastructure.security.RateLimitingService rateLimitingService;

        /**
         * Inscription d'un nouvel utilisateur
         */
        @PostMapping("/register")
        @Operation(summary = "Inscription d'un utilisateur", description = "Permet à un nouvel utilisateur de s'inscrire sur la plateforme")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscription réussie", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides fournies", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email déjà utilisé", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<AuthResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                log.info("Requête d'inscription reçue pour l'email: {}", request.getEmail());
                AuthResponse response = registerUseCase.execute(request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Inscription réussie"));
        }

        /**
         * Connexion d'un utilisateur
         */
        @PostMapping("/login")
        @Operation(summary = "Connexion d'un utilisateur", description = "Permet à un utilisateur de se connecter à la plateforme")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connexion réussie", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request) {
                log.info("Requête de connexion reçue pour l'email: {}", request.getEmail());
                AuthResponse response = loginUseCase.execute(request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Connexion réussie"));
        }

        /**
         * Rafraîchissement du token
         */
        @PostMapping("/refresh-token")
        @Operation(summary = "Rafraîchir le token", description = "Permet de rafraîchir le token d'accès avec le refresh token")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalide", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<AuthResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request) {
                log.info("Requête de rafraîchissement de token reçue");
                AuthResponse response = refreshTokenUseCase.execute(request);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Token rafraîchi avec succès"));
        }

        /**
         * Déconnexion d'un utilisateur
         */
        @PostMapping("/logout")
        @Operation(summary = "Déconnexion", description = "Permet à un utilisateur de se déconnecter de la plateforme")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Déconnexion réussie", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalide", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> logout(
                        @RequestBody RefreshTokenRequest request) {
                log.info("Requête de déconnexion reçue");
                logoutUseCase.execute(request.getRefreshToken());
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Déconnexion réussie"));
        }

        /**
         * Demande de réinitialisation de mot de passe
         */
        @PostMapping("/forgot-password")
        @Operation(summary = "Demande de réinitialisation de mot de passe", description = "Envoie un email avec un lien pour réinitialiser le mot de passe")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Instructions envoyées par email", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email non trouvé", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Trop de tentatives", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request,
                        jakarta.servlet.http.HttpServletRequest httpRequest) {

                String email = request.getEmail();
                String ipAddress = httpRequest.getRemoteAddr();
                String userAgent = httpRequest.getHeader("User-Agent");

                // Vérifier le rate limiting
                if (!rateLimitingService.tryConsume(email)) {
                        return ResponseEntity.status(429).body(
                                        com.app.easypharma_backend.application.common.dto.ApiResponse
                                                        .error("Trop de tentatives. Veuillez réessayer plus tard."));
                }

                // Exécuter la demande
                forgotPasswordUseCase.execute(email, ipAddress, userAgent);

                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Instructions envoyées par email"));
        }

        /**
         * Réinitialisation du mot de passe
         */
        @PostMapping("/reset-password")
        @Operation(summary = "Réinitialisation du mot de passe", description = "Permet de réinitialiser le mot de passe avec un token valide")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalide ou expiré", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<Void>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                resetPasswordUseCase.execute(request.getToken(), request.getNewPassword());
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(null,
                                                "Mot de passe réinitialisé avec succès"));
        }

        /**
         * Récupère le profil de l'utilisateur connecté
         */
        @GetMapping("/me")
        @Operation(summary = "Récupérer le profil utilisateur", description = "Permet à un utilisateur de récupérer ses informations de profil")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non autorisé", content = @Content)
        })
        public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<UserResponse>> getCurrentUser(
                        @RequestHeader("Authorization") String authHeader) {
                // Extraire l'email du token JWT
                String token = authHeader.replace("Bearer ", "");
                String email = jwtService.extractEmail(token);

                UserResponse response = getUserProfileUseCase.execute(email);
                return ResponseEntity.ok(
                                com.app.easypharma_backend.application.common.dto.ApiResponse.success(response,
                                                "Profil utilisateur récupéré"));
        }
}