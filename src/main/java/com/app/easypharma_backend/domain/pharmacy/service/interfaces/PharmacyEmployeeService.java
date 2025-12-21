package com.app.easypharma_backend.domain.pharmacy.service.interfaces;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.pharmacy.dto.AddEmployeeRequest;

import java.util.List;
import java.util.UUID;

public interface PharmacyEmployeeService {

    /**
     * Ajoute un employé existant à une pharmacie.
     * Le rôle doit être PHARMACY_EMPLOYEE ou DELIVERY.
     */
    UserResponse addEmployee(UUID pharmacyId, AddEmployeeRequest request);

    /**
     * Retire un employé d'une pharmacie (le remet en PATIENT et supprime le lien).
     */
    void removeEmployee(UUID pharmacyId, UUID employeeId);

    /**
     * Liste tous les employés d'une pharmacie.
     */
    List<UserResponse> getEmployees(UUID pharmacyId);

    /**
     * Liste les employés par rôle (ex: tous les livreurs).
     */
    List<UserResponse> getEmployeesByRole(UUID pharmacyId, UserRole role);
}
