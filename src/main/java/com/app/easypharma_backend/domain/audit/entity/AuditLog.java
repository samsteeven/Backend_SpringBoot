package com.app.easypharma_backend.domain.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking audit logs of critical actions in the system
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_entity_type", columnList = "entity_type"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * ID of the user who performed the action
     */
    @Column(nullable = true)
    private UUID userId;

    /**
     * Username for quick reference
     */
    @Column(nullable = true)
    private String username;

    /**
     * Action performed (e.g., CREATE_ORDER, UPDATE_INVENTORY, ASSIGN_DELIVERY)
     */
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * Type of entity affected (e.g., ORDER, MEDICATION, DELIVERY)
     */
    @Column(nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the affected entity
     */
    @Column(nullable = false)
    private UUID entityId;

    /**
     * JSON representation of changes made
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * IP address of the user
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * User agent (browser/device info)
     */
    @Column(length = 255)
    private String userAgent;

    /**
     * Timestamp of the action
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * Pharmacy ID if action is pharmacy-specific
     */
    private UUID pharmacyId;

    /**
     * Result of the action (SUCCESS, FAILURE)
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "SUCCESS";
}
