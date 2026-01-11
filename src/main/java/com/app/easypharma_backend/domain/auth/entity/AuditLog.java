package com.app.easypharma_backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String action; // Ex: "PASSWORD_RESET_REQUESTED", "PASSWORD_RESET_COMPLETED"

    @Column(nullable = false)
    private String email; // Email de l'utilisateur concerné

    @Column(name = "ip_address")
    private String ipAddress; // Adresse IP de la requête

    @Column(name = "user_agent")
    private String userAgent; // User-Agent du navigateur

    @Column(nullable = false)
    private String status; // "SUCCESS", "FAILURE", "RATE_LIMITED"

    @Column(length = 1000)
    private String details; // Détails supplémentaires (message d'erreur, etc.)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
