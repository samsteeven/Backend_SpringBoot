-- Migration V17: Ajout de la date d'expiration pour les médicaments en pharmacie
-- Description: Ajoute une colonne expiry_date à la table pharmacy_medications.

ALTER TABLE pharmacy_medications 
ADD COLUMN expiry_date DATE;

-- Optionnel: Ajouter un index pour faciliter la recherche de produits expirés
CREATE INDEX idx_pharmacy_medications_expiry_date ON pharmacy_medications(expiry_date);
