-- Migration V10: Tables pour statistiques et litiges
-- Description: Ajoute search_logs pour les stats de recherche et return_requests pour la gestion des litiges.

-- 1. Table des logs de recherche
CREATE TABLE search_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    query VARCHAR(255) NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_logs_timestamp ON search_logs(searched_at);
CREATE INDEX idx_search_logs_query ON search_logs(query);

-- 2. Table des demandes de retour (litiges)
CREATE TABLE return_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id), -- Le patient qui demande
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_returns_status ON return_requests(status);
