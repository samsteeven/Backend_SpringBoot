package com.app.easypharma_backend.domain.audit.service;

import com.app.easypharma_backend.domain.audit.dto.AuditLogDTO;
import com.app.easypharma_backend.domain.audit.entity.AuditLog;
import com.app.easypharma_backend.domain.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing audit logs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an action asynchronously
     */
    @Async
    public void logAction(
            UUID userId,
            String username,
            String action,
            String entityType,
            UUID entityId,
            Map<String, Object> details,
            String ipAddress,
            String userAgent,
            UUID pharmacyId) {
        try {
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(detailsJson)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .pharmacyId(pharmacyId)
                    .status("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by user {}", action, username);
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit log details", e);
        } catch (Exception e) {
            log.error("Error saving audit log", e);
        }
    }

    /**
     * Log a failed action
     */
    @Async
    public void logFailedAction(
            UUID userId,
            String username,
            String action,
            String entityType,
            UUID entityId,
            String errorMessage,
            String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(errorMessage)
                    .ipAddress(ipAddress)
                    .status("FAILURE")
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Failed audit log created: {} by user {}", action, username);
        } catch (Exception e) {
            log.error("Error saving failed audit log", e);
        }
    }

    /**
     * Get all audit logs (Super Admin only)
     */
    public Page<AuditLogDTO> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs for a user
     */
    public Page<AuditLogDTO> getUserLogs(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs for a pharmacy
     */
    public Page<AuditLogDTO> getPharmacyLogs(UUID pharmacyId, Pageable pageable) {
        return auditLogRepository.findByPharmacyIdOrderByTimestampDesc(pharmacyId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs by action type
     */
    public Page<AuditLogDTO> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs for a specific entity
     */
    public Page<AuditLogDTO> getEntityLogs(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs within a date range
     */
    public Page<AuditLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get audit logs for a pharmacy within a date range
     */
    public Page<AuditLogDTO> getPharmacyLogsByDateRange(
            UUID pharmacyId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.findByPharmacyAndDateRange(pharmacyId, startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Map entity to DTO
     */
    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .timestamp(auditLog.getTimestamp())
                .pharmacyId(auditLog.getPharmacyId())
                .status(auditLog.getStatus())
                .build();
    }
}
