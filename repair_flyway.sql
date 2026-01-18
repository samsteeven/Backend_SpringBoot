-- Script de réparation pour Flyway
-- À exécuter dans pgAdmin ou tout autre client PostgreSQL

-- 1. Supprimer les entrées de migration échouées
DELETE FROM flyway_schema_history WHERE version = '4' AND success = false;

-- 2. Vérifier l'état actuel
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
