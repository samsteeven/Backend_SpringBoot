package com.app.easypharma_backend.domain.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for audit log entries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private UUID pharmacyId;
    private String status;
}
