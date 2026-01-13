package com.app.easypharma_backend.domain.audit.repository;

import com.app.easypharma_backend.domain.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for audit logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

        /**
         * Find all logs ordered by timestamp
         */
        Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

        /**
         * Find all logs for a specific user
         */
        Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

        /**
         * Find all logs for a specific action
         */
        Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

        /**
         * Find all logs for a specific entity
         */
        Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
                        String entityType,
                        UUID entityId,
                        Pageable pageable);

        /**
         * Find all logs for a pharmacy
         */
        Page<AuditLog> findByPharmacyIdOrderByTimestampDesc(UUID pharmacyId, Pageable pageable);

        /**
         * Find logs within a date range
         */
        @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
        Page<AuditLog> findByDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find logs by pharmacy and date range
         */
        @Query("SELECT a FROM AuditLog a WHERE a.pharmacyId = :pharmacyId AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
        Page<AuditLog> findByPharmacyAndDateRange(
                        @Param("pharmacyId") UUID pharmacyId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Count logs by action
         */
        long countByAction(String action);

        /**
         * Count logs for a user
         */
        long countByUserId(UUID userId);
}
