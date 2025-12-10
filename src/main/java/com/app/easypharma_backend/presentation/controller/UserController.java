package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.ChangeUserRoleRequest;
import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.usecase.*;
import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.application.common.dto.PageResponse;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserByIdUseCase updateUserByIdUseCase;
    private final DeleteUserByIdUseCase deleteUserByIdUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final JwtService jwtService;

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

    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateUserRequest request) {
        // Extraire l'email du token JWT
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        
        UserResponse response = updateUserProfileUseCase.execute(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profil utilisateur mis à jour"));
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Utilisateur récupéré"));
    }

    /**
     * Supprime le compte de l'utilisateur connecté
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        // Extraire l'email du token JWT
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        
        deleteUserUseCase.execute(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Compte utilisateur supprimé"));
    }

    /**
     * Liste tous les utilisateurs (réservé aux administrateurs)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserResponse> response = listUsersUseCase.execute(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Liste des utilisateurs"));
    }

    /**
     * Met à jour un utilisateur par ID (réservé aux administrateurs)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserById(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = updateUserByIdUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Utilisateur mis à jour"));
    }

    /**
     * Supprime un utilisateur par ID (réservé aux administrateurs)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable UUID id) {
        deleteUserByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Utilisateur supprimé"));
    }

    /**
     * Change le rôle d'un utilisateur (réservé aux administrateurs)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        UserResponse response = changeUserRoleUseCase.execute(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(response, "Rôle utilisateur modifié"));
    }
}