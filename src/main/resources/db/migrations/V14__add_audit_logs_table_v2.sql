-- Migration V14: Recréation correcte de la table audit_logs
-- Description: Supprime l'ancienne table obsolète et crée le nouveau schéma aligné avec l'entité JPA AuditLog.

-- Suppression de l'ancienne table si elle existe (pour éviter le conflit de schéma de la migration V9 précédente)
DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pharmacy_id UUID,
    status VARCHAR(20) DEFAULT 'SUCCESS'
);

-- Index pour améliorer les performances des requêtes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_pharmacy_id ON audit_logs(pharmacy_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_status ON audit_logs(status);
