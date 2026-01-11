package com.app.easypharma_backend.domain.auth.repository;

import com.app.easypharma_backend.domain.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Trouve tous les logs d'audit pour un email donné
     */
    List<AuditLog> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * Trouve tous les logs d'audit pour une action donnée
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Trouve tous les logs d'audit dans une période donnée
     */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
