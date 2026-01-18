-- =============================================================================
-- EasyPharma - Full Testing Seed Script
-- Description: Populates all tables with realistic data for end-to-end testing.
-- Usage: Run this script against your PostgreSQL database.
-- =============================================================================

DO $$
DECLARE
    -- User IDs
    v_super_admin_id UUID := '11111111-1111-1111-1111-111111111111';
    v_pharmacy_admin_id UUID := '22222222-2222-2222-2222-222222222222';
    v_pharmacist2_id UUID := '22222222-2222-2222-2222-222222222223';
    v_patient_id UUID := '33333333-3333-3333-3333-333333333333';
    v_patient2_id UUID := '33333333-3333-3333-3333-333333333334';
    v_delivery_id UUID := '44444444-4444-4444-4444-444444444444';
    
    -- Pharmacy IDs
    v_pharmacy_sun_id UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
    v_pharmacy_health_id UUID := 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
    
    -- Medication IDs (taking some from V4 or creating new ones)
    v_med_doliprane_id UUID := 'd1111111-1111-1111-1111-111111111111';
    v_med_amoxi_id UUID := 'd2222222-2222-2222-2222-222222222222';
    v_med_malaria_id UUID := 'd3333333-3333-3333-3333-333333333333';
    
    -- Order IDs
    v_order1_id UUID := 'e1111111-1111-1111-1111-111111111111';
    v_order2_id UUID := 'e2222222-2222-2222-2222-222222222222';
BEGIN
    -- CLEANUP (Optional: Uncomment if you want a fresh start)
    -- DELETE FROM audit_logs; DELETE FROM reviews; DELETE FROM deliveries; DELETE FROM payments;
    -- DELETE FROM order_items; DELETE FROM orders; DELETE FROM pharmacy_medications;
    -- DELETE FROM medications; DELETE FROM pharmacies; DELETE FROM users;

    -- 1. USERS
    -- Super Admin
    INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified)
    VALUES (v_super_admin_id, 'superadmin@easypharma.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Super', 'Admin', '+237600000001', 'SUPER_ADMIN', TRUE, TRUE)
    ON CONFLICT (email) DO NOTHING;

    -- Pharmacy Admin 1
    INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified)
    VALUES (v_pharmacy_admin_id, 'admin.soleil@easypharma.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Moussa', 'Pharmacien', '+237600000002', 'PHARMACY_ADMIN', TRUE, TRUE)
    ON CONFLICT (email) DO NOTHING;

    -- Pharmacy Admin 2
    INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified)
    VALUES (v_pharmacist2_id, 'admin.sante@easypharma.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Sante', '+237600000003', 'PHARMACY_ADMIN', TRUE, TRUE)
    ON CONFLICT (email) DO NOTHING;

    -- Patient 1
    INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified, city, address)
    VALUES (v_patient_id, 'patient.test@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Paul', 'Biya', '+237600000004', 'PATIENT', TRUE, TRUE, 'Yaoundé', 'Bastos')
    ON CONFLICT (email) DO NOTHING;

    -- Delivery Person
    INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified)
    VALUES (v_delivery_id, 'livreur.test@easypharma.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Yaya', 'Moussa', '+237600000005', 'DELIVERY', TRUE, TRUE)
    ON CONFLICT (email) DO NOTHING;

    -- 2. PHARMACIES
    -- Pharmacy 1 (APPROVED)
    INSERT INTO pharmacies (id, user_id, name, license_number, address, city, phone, latitude, longitude, status, validated_at, license_document_url)
    VALUES (v_pharmacy_sun_id, v_pharmacy_admin_id, 'Pharmacie du Soleil', 'LIC-237-001', 'Bastos, face station', 'Yaoundé', '+237699999991', 3.8480, 11.5021, 'APPROVED', NOW(), 'http://docs.com/lic1.pdf')
    ON CONFLICT (license_number) DO NOTHING;

    -- Pharmacy 2 (APPROVED)
    INSERT INTO pharmacies (id, user_id, name, license_number, address, city, phone, latitude, longitude, status, validated_at, license_document_url)
    VALUES (v_pharmacy_health_id, v_pharmacist2_id, 'Pharmacie de la Santé', 'LIC-237-002', 'Akwa, Rue de la Joie', 'Douala', '+237699999992', 4.0511, 9.7085, 'APPROVED', NOW(), 'http://docs.com/lic2.pdf')
    ON CONFLICT (license_number) DO NOTHING;

    -- Link users to pharmacies
    UPDATE users SET pharmacy_id = v_pharmacy_sun_id WHERE id = v_pharmacy_admin_id;
    UPDATE users SET pharmacy_id = v_pharmacy_health_id WHERE id = v_pharmacist2_id;

    -- 3. MEDICATIONS
    INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription)
    VALUES 
        (v_med_doliprane_id, 'Doliprane 1g', 'Paracétamol', 'ANTALGIQUE', 'Soulage la douleur et la fièvre', '1000mg', FALSE),
        (v_med_amoxi_id, 'Amoxicilline 500mg', 'Amoxicilline', 'ANTIBIOTIQUE', 'Antibiotique large spectre', '500mg', TRUE),
        (v_med_malaria_id, 'Coartem', 'Artéméther + Luméfantrine', 'ANTIPALUDEEN', 'Traitement du paludisme', '20/120mg', TRUE)
    ON CONFLICT DO NOTHING;

    -- 4. PHARMACY INVENTORY (STOCK)
    INSERT INTO pharmacy_medications (id, pharmacy_id, medication_id, price, stock_quantity, is_available)
    VALUES 
        (gen_random_uuid(), v_pharmacy_sun_id, v_med_doliprane_id, 1500.00, 100, TRUE),
        (gen_random_uuid(), v_pharmacy_sun_id, v_med_amoxi_id, 3500.00, 50, TRUE),
        (gen_random_uuid(), v_pharmacy_health_id, v_med_doliprane_id, 1450.00, 200, TRUE),
        (gen_random_uuid(), v_pharmacy_health_id, v_med_malaria_id, 5500.00, 20, TRUE)
    ON CONFLICT DO NOTHING;

    -- 5. ORDERS
    -- Order 1: DELIVERED
    INSERT INTO orders (id, order_number, patient_id, pharmacy_id, total_amount, status, delivery_address, delivery_city, delivery_phone, delivered_at)
    VALUES (v_order1_id, 'ORD-2024-001', v_patient_id, v_pharmacy_sun_id, 5000.00, 'DELIVERED', 'Bastos, Yaoundé', 'Yaoundé', '+237600000004', NOW() - INTERVAL '1 day')
    ON CONFLICT DO NOTHING;

    -- Order 2: PENDING
    INSERT INTO orders (id, order_number, patient_id, pharmacy_id, total_amount, status, delivery_address, delivery_city, delivery_phone)
    VALUES (v_order2_id, 'ORD-2024-002', v_patient_id, v_pharmacy_health_id, 1450.00, 'PENDING', 'Bonapriso, Douala', 'Douala', '+237600000004')
    ON CONFLICT DO NOTHING;

    -- 6. ORDER ITEMS
    INSERT INTO order_items (id, order_id, medication_id, quantity, unit_price, subtotal)
    VALUES 
        (gen_random_uuid(), v_order1_id, v_med_doliprane_id, 1, 1500.00, 1500.00),
        (gen_random_uuid(), v_order1_id, v_med_amoxi_id, 1, 3500.00, 3500.00),
        (gen_random_uuid(), v_order2_id, v_med_doliprane_id, 1, 1450.00, 1450.00)
    ON CONFLICT DO NOTHING;

    -- 7. PAYMENTS
    INSERT INTO payments (id, order_id, payment_method, amount, status, transaction_id, paid_at)
    VALUES (gen_random_uuid(), v_order1_id, 'MTN_MOMO', 5000.00, 'SUCCESS', 'TX-MOMO-999', NOW() - INTERVAL '1 day')
    ON CONFLICT DO NOTHING;

    -- 8. DELIVERIES
    INSERT INTO deliveries (id, order_id, delivery_person_id, status, photo_proof_url, delivered_at)
    VALUES (gen_random_uuid(), v_order1_id, v_delivery_id, 'DELIVERED', 'http://example.com/delivery.jpg', NOW() - INTERVAL '1 day')
    ON CONFLICT DO NOTHING;

    -- 9. REVIEWS
    INSERT INTO reviews (id, patient_id, pharmacy_id, order_id, rating, comment, status)
    VALUES (gen_random_uuid(), v_patient_id, v_pharmacy_sun_id, v_order1_id, 5, 'Service rapide et efficace !', 'APPROVED')
    ON CONFLICT DO NOTHING;

    -- 10. AUDIT LOGS
    INSERT INTO audit_logs (id, user_id, username, action, entity_type, entity_id, status, details, timestamp)
    VALUES 
        (gen_random_uuid(), v_super_admin_id, 'superadmin@easypharma.cm', 'APPROVE_PHARMACY', 'PHARMACY', v_pharmacy_sun_id, 'SUCCESS', 'Approbation de la pharmacie du Soleil', NOW() - INTERVAL '2 days'),
        (gen_random_uuid(), v_pharmacy_admin_id, 'admin.soleil@easypharma.cm', 'UPDATE_STOCK', 'MEDICATION', v_med_doliprane_id, 'SUCCESS', 'Mise à jour du stock Doliprane (+50)', NOW() - INTERVAL '1 hour');

END $$;

-- Result
SELECT 'Données de test injectées avec succès !' as status;
