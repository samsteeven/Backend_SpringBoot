package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.employee.dto.EmployeePermissionDTO;
import com.app.easypharma_backend.domain.employee.service.EmployeePermissionService;
import com.app.easypharma_backend.application.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for managing employee permissions
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Permissions", description = "Endpoints for managing employee permissions")
public class EmployeePermissionController {

    private final EmployeePermissionService permissionService;
    private final com.app.easypharma_backend.domain.employee.service.EmployeeReportService reportService;
    private final com.app.easypharma_backend.domain.employee.service.EmployeeStatisticsService statisticsService;

    /**
     * Get permissions for a specific employee
     */
    @GetMapping("/{employeeId}/permissions")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or (hasRole('PHARMACY_EMPLOYEE') and #employeeId.toString() == authentication.principal.id)")
    @Operation(summary = "Get employee permissions", description = "Retrieve permissions for a specific employee")
    public ResponseEntity<ApiResponse<EmployeePermissionDTO>> getPermissions(
            @PathVariable UUID employeeId,
            Authentication authentication) {

        log.info("Fetching permissions for employee: {}", employeeId);
        EmployeePermissionDTO permissions = permissionService.getPermissions(employeeId);

        return ResponseEntity.ok(ApiResponse.success(permissions, "Permissions retrieved successfully"));
    }

    /**
     * Update permissions for an employee (Admin only)
     */
    @PutMapping("/{employeeId}/permissions")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Update employee permissions", description = "Update permissions for a specific employee (Admin only)")
    public ResponseEntity<ApiResponse<EmployeePermissionDTO>> updatePermissions(
            @PathVariable UUID employeeId,
            @RequestBody EmployeePermissionDTO permissionDTO,
            Authentication authentication) {

        UUID adminId = UUID.fromString(authentication.getName());
        log.info("Admin {} updating permissions for employee {}", adminId, employeeId);

        EmployeePermissionDTO updated = permissionService.updatePermissions(employeeId, permissionDTO, adminId);

        return ResponseEntity.ok(ApiResponse.success(updated, "Permissions updated successfully"));
    }

    /**
     * Check if an employee has a specific permission
     */
    @GetMapping("/{employeeId}/permissions/check")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACY_EMPLOYEE')")
    @Operation(summary = "Check permission", description = "Check if an employee has a specific permission")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(
            @PathVariable UUID employeeId,
            @RequestParam String permissionType,
            Authentication authentication) {

        boolean hasPermission = permissionService.hasPermission(employeeId, permissionType);

        return ResponseEntity.ok(ApiResponse.success(hasPermission,
                "Permission check completed: " + (hasPermission ? "Granted" : "Denied")));
    }

    /**
     * Export employee report as CSV
     */
    @GetMapping("/{employeeId}/report")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or (hasRole('PHARMACY_EMPLOYEE') and #employeeId.toString() == authentication.principal.id)")
    @Operation(summary = "Export employee report", description = "Generate and download CSV report for employee activity")
    public ResponseEntity<byte[]> exportEmployeeReport(
            @PathVariable UUID employeeId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            Authentication authentication) {

        log.info("Generating report for employee {} from {} to {}", employeeId, startDate, endDate);

        byte[] csvData = reportService.generateEmployeeReport(employeeId, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=employee-report-" + employeeId + ".csv")
                .body(csvData);
    }

    /**
     * Get employee statistics
     */
    @GetMapping("/{employeeId}/statistics")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or (hasRole('PHARMACY_EMPLOYEE') and #employeeId.toString() == authentication.principal.id)")
    @Operation(summary = "Get employee statistics", description = "Retrieve performance statistics for an employee")
    public ResponseEntity<com.app.easypharma_backend.application.common.dto.ApiResponse<com.app.easypharma_backend.domain.employee.dto.EmployeeStatisticsDTO>> getStatistics(
            @PathVariable UUID employeeId,
            Authentication authentication) {

        log.info("Fetching statistics for employee {}", employeeId);
        com.app.easypharma_backend.domain.employee.dto.EmployeeStatisticsDTO stats = statisticsService
                .getStatistics(employeeId);

        return ResponseEntity.ok(com.app.easypharma_backend.application.common.dto.ApiResponse.success(stats,
                "Statistics retrieved successfully"));
    }
}
