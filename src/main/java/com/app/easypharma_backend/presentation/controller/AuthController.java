package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.*;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.usecase.ForgotPasswordUseCase;
import com.app.easypharma_backend.application.auth.usecase.GetUserProfileUseCase;
import com.app.easypharma_backend.application.auth.usecase.LoginUseCase;
import com.app.easypharma_backend.application.auth.usecase.LogoutUseCase;
import com.app.easypharma_backend.application.auth.usecase.RefreshTokenUseCase;
import com.app.easypharma_backend.application.auth.usecase.RegisterUseCase;
import com.app.easypharma_backend.application.auth.usecase.ResetPasswordUseCase;
import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.infrastructure.security.JwtService;
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
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final JwtService jwtService;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Requête d'inscription reçue pour l'email: {}", request.getEmail());
        AuthResponse response = registerUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Inscription réussie"));
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Requête de connexion reçue pour l'email: {}", request.getEmail());
        AuthResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Connexion réussie"));
    }

    /**
     * Rafraîchissement du token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Requête de rafraîchissement de token reçue");
        AuthResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token rafraîchi avec succès"));
    }

    /**
     * Déconnexion d'un utilisateur
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest request) {
        log.info("Requête de déconnexion reçue");
        logoutUseCase.execute(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Déconnexion réussie"));
    }

    /**
     * Demande de réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Instructions envoyées par email"));
    }

    /**
     * Réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Mot de passe réinitialisé avec succès"));
    }

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        // Extraire l'email du token JWT
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        
        UserResponse response = getUserProfileUseCase.execute(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Profil utilisateur récupéré"));
    }
}