package com.app.easypharma_backend.domain.auth.service;

import com.app.easypharma_backend.domain.auth.entity.AuditLog;
import com.app.easypharma_backend.domain.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Enregistre un événement d'audit
     */
    @Transactional
    public void logEvent(String action, String email, String ipAddress, String userAgent, String status,
            String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .email(email)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(status)
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} for email: {}", action, email);
        } catch (Exception e) {
            // Ne pas bloquer le flux principal en cas d'erreur d'audit
            log.error("Failed to create audit log for action: {} and email: {}", action, email, e);
        }
    }

    /**
     * Enregistre une tentative de réinitialisation de mot de passe
     */
    public void logPasswordResetRequest(String email, String ipAddress, String userAgent, boolean success,
            String details) {
        String status = success ? "SUCCESS" : "FAILURE";
        logEvent("PASSWORD_RESET_REQUESTED", email, ipAddress, userAgent, status, details);
    }

    /**
     * Enregistre une réinitialisation de mot de passe réussie
     */
    public void logPasswordResetCompleted(String email, String ipAddress, String userAgent, boolean success,
            String details) {
        String status = success ? "SUCCESS" : "FAILURE";
        logEvent("PASSWORD_RESET_COMPLETED", email, ipAddress, userAgent, status, details);
    }

    /**
     * Enregistre un dépassement de limite de taux
     */
    public void logRateLimitExceeded(String email, String ipAddress, String userAgent) {
        logEvent("PASSWORD_RESET_REQUESTED", email, ipAddress, userAgent, "RATE_LIMITED", "Too many attempts");
    }
}
