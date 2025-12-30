-- Migration V8: Ajout de données de test (Pharmacie approuvée + Inventaire)
-- Description: Crée une pharmacie "EasyPharma Test" approuvée et lui assigne du stock pour les tests mobiles.

-- 1. Création d'un utilisateur Pharmacien (si non existant via test flow)
-- On utilise un UUID fixe pour plus de simplicité dans les scripts de démo
DO $$ 
DECLARE 
    v_admin_user_id UUID := '0c4cf416-591d-40f8-b9d6-c37dabff308c';
    v_pharmacy_id UUID := '1c4cf416-591d-40f8-b9d6-c37dabff308e';
BEGIN 
    -- Vérifier si l'utilisateur existe déjà (admin@easypharma.cm est créé en V1, mais on crée un pharmacist-test)
    IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'pharmacien-test@easypharma.cm') THEN
        INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified, created_at, updated_at)
        VALUES (
            v_admin_user_id,
            'pharmacien-test@easypharma.cm',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
            'Jean',
            'Pharmacien',
            '+237611111111',
            'PHARMACY_ADMIN',
            TRUE,
            TRUE,
            NOW(),
            NOW()
        );
    END IF;

    -- 2. Création de la Pharmacie (Approuvée)
    IF NOT EXISTS (SELECT 1 FROM pharmacies WHERE name = 'Pharmacie du Soleil (Test)') THEN
        INSERT INTO pharmacies (id, user_id, name, license_number, address, city, phone, latitude, longitude, description, status, license_document_url, validated_at, created_at, updated_at)
        VALUES (
            v_pharmacy_id,
            v_admin_user_id,
            'Pharmacie du Soleil (Test)',
            'LIC-TEST-001',
            'Rue de la Joie, Akwa',
            'Douala',
            '+237699999999',
            4.0483, -- Douala Lat
            9.7042,  -- Douala Lon
            'Pharmacie de test pour le développement mobile',
            'APPROVED',
            'http://example.com/license.pdf',
            NOW(),
            NOW(),
            NOW()
        );

        -- Lier l'utilisateur à sa pharmacie
        UPDATE users SET pharmacy_id = v_pharmacy_id WHERE id = v_admin_user_id;
    END IF;

    -- 3. Ajout de Médicaments à l'inventaire (Pharmacy Medication)
    -- On prend les IDs des médicaments créés en V4 (on les sélectionne par nom car gen_random_uuid a été utilisé)
    INSERT INTO pharmacy_medications (id, pharmacy_id, medication_id, price, stock_quantity, is_available, created_at, updated_at)
    SELECT 
        gen_random_uuid(),
        v_pharmacy_id,
        m.id,
        CASE 
            WHEN m.name LIKE '%Doliprane%' THEN 1500.00
            WHEN m.name LIKE '%Amoxicilline%' THEN 3500.00
            WHEN m.name LIKE '%Artéméther%' THEN 5500.00
            ELSE 2000.00
        END,
        100, -- stock initial
        TRUE,
        NOW(),
        NOW()
    FROM medications m
    WHERE NOT EXISTS (
        SELECT 1 FROM pharmacy_medications pm 
        WHERE pm.pharmacy_id = v_pharmacy_id AND pm.medication_id = m.id
    );

END $$;
