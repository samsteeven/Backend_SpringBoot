-- Migration pour ajouter les champs manquants à orders pour meilleur tracking de livraison
-- Vérifier si les champs existent avant de les ajouter

-- Ajouter un champ pour tracker la livraison assignée (optionnel, on peut utiliser deliveries directement)
-- Mais pour optimisation, on peut avoir un index sur order_id dans deliveries

-- Ajouter un statut spécifique pour la livraison si besoin (pickup ready, out for delivery, etc)
-- Pour l'instant, on utilise les statuts existants: READY, IN_DELIVERY, DELIVERED

-- Créer un index pour les commandes en attente de livraison (READY)
CREATE INDEX IF NOT EXISTS idx_orders_status_ready ON orders(status) WHERE status = 'READY';

-- Créer un index pour chercher rapidement les commandes par pharmacie et statut
CREATE INDEX IF NOT EXISTS idx_orders_pharmacy_status ON orders(pharmacy_id, status);
