-- Migration V9: Extension de la table reviews pour inclure les livreurs
-- Description: Ajoute les colonnes pour noter le livreur et assure la structure pour la modération.

ALTER TABLE reviews 
ADD COLUMN courier_id UUID REFERENCES users(id),
ADD COLUMN courier_rating INTEGER CHECK (courier_rating IS NULL OR (courier_rating >= 1 AND courier_rating <= 5)),
ADD COLUMN courier_comment TEXT;

-- Index pour faciliter la recherche des avis par livreur
CREATE INDEX idx_reviews_courier_id ON reviews(courier_id);

-- Commentaire pour expliquer la logique
COMMENT ON COLUMN reviews.rating IS 'Note attribuée à la pharmacie (1-5)';
COMMENT ON COLUMN reviews.courier_rating IS 'Note attribuée au livreur (1-5)';
COMMENT ON COLUMN reviews.status IS 'Statut de modération (PENDING, APPROVED, REJECTED)';
