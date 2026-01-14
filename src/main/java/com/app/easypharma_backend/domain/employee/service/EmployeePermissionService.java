package com.app.easypharma_backend.domain.employee.service;

import com.app.easypharma_backend.domain.employee.dto.EmployeePermissionDTO;
import com.app.easypharma_backend.domain.employee.entity.EmployeePermission;
import com.app.easypharma_backend.domain.employee.repository.EmployeePermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing employee permissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeePermissionService {

    private final EmployeePermissionRepository permissionRepository;

    /**
     * Get permissions for an employee. If none exist, create default permissions.
     */
    public EmployeePermissionDTO getPermissions(UUID employeeId) {
        EmployeePermission permission = permissionRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> createDefaultPermissions(employeeId));

        return mapToDTO(permission);
    }

    /**
     * Update permissions for an employee
     */
    @Transactional
    public EmployeePermissionDTO updatePermissions(UUID employeeId, EmployeePermissionDTO dto, UUID modifiedBy) {
        EmployeePermission permission = permissionRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Permissions not found for employee: " + employeeId));

        // Update permissions only if provided
        if (dto.getCanPrepareOrders() != null)
            permission.setCanPrepareOrders(dto.getCanPrepareOrders());
        if (dto.getCanAssignDeliveries() != null)
            permission.setCanAssignDeliveries(dto.getCanAssignDeliveries());
        if (dto.getCanViewStatistics() != null)
            permission.setCanViewStatistics(dto.getCanViewStatistics());
        if (dto.getCanManageInventory() != null)
            permission.setCanManageInventory(dto.getCanManageInventory());
        if (dto.getCanViewCustomerInfo() != null)
            permission.setCanViewCustomerInfo(dto.getCanViewCustomerInfo());
        if (dto.getCanProcessPayments() != null)
            permission.setCanProcessPayments(dto.getCanProcessPayments());
        permission.setLastModifiedBy(modifiedBy);

        EmployeePermission saved = permissionRepository.save(permission);
        log.info("Updated permissions for employee {} by admin {}", employeeId, modifiedBy);

        return mapToDTO(saved);
    }

    /**
     * Create default permissions for a new employee
     */
    @Transactional
    public EmployeePermission createDefaultPermissions(UUID employeeId) {
        EmployeePermission permission = EmployeePermission.builder()
                .employeeId(employeeId)
                .canPrepareOrders(true)
                .canAssignDeliveries(true)
                .canViewStatistics(false)
                .canManageInventory(false)
                .canViewCustomerInfo(true)
                .canProcessPayments(true)
                .build();

        EmployeePermission saved = permissionRepository.save(permission);
        log.info("Created default permissions for employee {}", employeeId);

        return saved;
    }

    /**
     * Check if an employee has a specific permission
     */
    public boolean hasPermission(UUID employeeId, String permissionType) {
        EmployeePermission permission = permissionRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> createDefaultPermissions(employeeId));

        return switch (permissionType.toUpperCase()) {
            case "PREPARE_ORDERS" -> permission.getCanPrepareOrders();
            case "ASSIGN_DELIVERIES" -> permission.getCanAssignDeliveries();
            case "VIEW_STATISTICS" -> permission.getCanViewStatistics();
            case "MANAGE_INVENTORY" -> permission.getCanManageInventory();
            case "VIEW_CUSTOMER_INFO" -> permission.getCanViewCustomerInfo();
            case "PROCESS_PAYMENTS" -> permission.getCanProcessPayments();
            default -> false;
        };
    }

    /**
     * Delete permissions for an employee
     */
    @Transactional
    public void deletePermissions(UUID employeeId) {
        permissionRepository.deleteByEmployeeId(employeeId);
        log.info("Deleted permissions for employee {}", employeeId);
    }

    /**
     * Map entity to DTO
     */
    private EmployeePermissionDTO mapToDTO(EmployeePermission permission) {
        return EmployeePermissionDTO.builder()
                .id(permission.getId())
                .employeeId(permission.getEmployeeId())
                .canPrepareOrders(permission.getCanPrepareOrders())
                .canAssignDeliveries(permission.getCanAssignDeliveries())
                .canViewStatistics(permission.getCanViewStatistics())
                .canManageInventory(permission.getCanManageInventory())
                .canViewCustomerInfo(permission.getCanViewCustomerInfo())
                .canProcessPayments(permission.getCanProcessPayments())
                .build();
    }
}
