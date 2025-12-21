package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.pharmacy.dto.AddEmployeeRequest;
import com.app.easypharma_backend.domain.pharmacy.service.interfaces.PharmacyEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacyId}/employees")
@RequiredArgsConstructor
@Tag(name = "Gestion des employés", description = "Endpoints pour gérer le personnel de la pharmacie")
public class PharmacyEmployeeController {

    private final PharmacyEmployeeService pharmacyEmployeeService;

    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Ajouter un employé", description = "Permet à l'admin de la pharmacie d'ajouter un employé (Pharmacien ou Livreur) via son email")
    public ResponseEntity<ApiResponse<UserResponse>> addEmployee(
            @Parameter(description = "ID de la pharmacie") @PathVariable UUID pharmacyId,
            @Valid @RequestBody AddEmployeeRequest request) {

        UserResponse response = pharmacyEmployeeService.addEmployee(pharmacyId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employé ajouté avec succès"));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Retirer un employé", description = "Permet à l'admin de la pharmacie de retirer un employé")
    public ResponseEntity<ApiResponse<Void>> removeEmployee(
            @Parameter(description = "ID de la pharmacie") @PathVariable UUID pharmacyId,
            @Parameter(description = "ID de l'employé") @PathVariable UUID employeeId) {

        pharmacyEmployeeService.removeEmployee(pharmacyId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Employé retiré avec succès"));
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    @Operation(summary = "Lister les employés", description = "Retourne la liste de tous les employés de la pharmacie")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getEmployees(
            @Parameter(description = "ID de la pharmacie") @PathVariable UUID pharmacyId) {

        List<UserResponse> employees = pharmacyEmployeeService.getEmployees(pharmacyId);
        return ResponseEntity.ok(ApiResponse.success(employees, "Liste des employés récupérée"));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    @Operation(summary = "Lister les employés par rôle", description = "Retourne la liste des employés ayant un rôle spécifique (ex: DELIVERY)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getEmployeesByRole(
            @Parameter(description = "ID de la pharmacie") @PathVariable UUID pharmacyId,
            @Parameter(description = "Rôle (PHARMACY_EMPLOYEE ou DELIVERY)") @PathVariable UserRole role) {

        List<UserResponse> employees = pharmacyEmployeeService.getEmployeesByRole(pharmacyId, role);
        return ResponseEntity.ok(ApiResponse.success(employees, "Liste des employés par rôle récupérée"));
    }
}
