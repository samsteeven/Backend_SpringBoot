package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.domain.audit.dto.AuditLogDTO;
import com.app.easypharma_backend.domain.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller for audit log management (Admin only)
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Endpoints for viewing audit logs (Admin only)")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get all audit logs (Super Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all audit logs", description = "Retrieve all system audit logs (Super Admin only)")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getAllLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "All audit logs retrieved successfully"));
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Get user audit logs", description = "Retrieve audit logs for a specific user")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getUserLogs(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getUserLogs(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "User audit logs retrieved successfully"));
    }

    /**
     * Get audit logs for a pharmacy
     */
    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Get pharmacy audit logs", description = "Retrieve audit logs for a specific pharmacy")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getPharmacyLogs(
            @PathVariable UUID pharmacyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getPharmacyLogs(pharmacyId, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "Pharmacy audit logs retrieved successfully"));
    }

    /**
     * Get audit logs by action type
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get logs by action", description = "Retrieve audit logs filtered by action type")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getLogsByAction(action, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs by action retrieved successfully"));
    }

    /**
     * Get audit logs for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Get entity audit logs", description = "Retrieve audit logs for a specific entity")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getEntityLogs(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getEntityLogs(entityType, entityId, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "Entity audit logs retrieved successfully"));
    }

    /**
     * Get audit logs within a date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get logs by date range", description = "Retrieve audit logs within a specific date range")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getLogsByDateRange(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs by date range retrieved successfully"));
    }

    /**
     * Get audit logs for a pharmacy within a date range
     */
    @GetMapping("/pharmacy/{pharmacyId}/date-range")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PHARMACY_ADMIN')")
    @Operation(summary = "Get pharmacy logs by date range", description = "Retrieve pharmacy audit logs within a date range")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getPharmacyLogsByDateRange(
            @PathVariable UUID pharmacyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.getPharmacyLogsByDateRange(pharmacyId, startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(logs, "Pharmacy audit logs by date range retrieved successfully"));
    }
}
