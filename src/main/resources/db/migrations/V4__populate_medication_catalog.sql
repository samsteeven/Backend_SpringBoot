-- Migration V4: Pré-remplissage du catalogue de médicaments
-- Description: Ajoute les médicaments courants pour faciliter le démarrage

-- Analgésiques et Antipyrétiques
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Paracétamol 500mg', 'Paracétamol', 'ANTALGIQUE', 'Analgésique et antipyrétique pour douleurs légères à modérées', '500mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Paracétamol 1000mg', 'Paracétamol', 'ANTALGIQUE', 'Analgésique et antipyrétique pour douleurs modérées', '1000mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Ibuprofène 400mg', 'Ibuprofène', 'ANTIINFLAMMATOIRE', 'Anti-inflammatoire non stéroïdien (AINS)', '400mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Aspirine 500mg', 'Acide acétylsalicylique', 'ANTALGIQUE', 'Analgésique, antipyrétique et antiagrégant plaquettaire', '500mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Doliprane 1000mg', 'Paracétamol', 'ANTALGIQUE', 'Traitement symptomatique de la douleur et de la fièvre', '1000mg', false, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Antibiotiques
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Amoxicilline 500mg', 'Amoxicilline', 'ANTIBIOTIQUE', 'Antibiotique de la famille des pénicillines', '500mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Amoxicilline 1g', 'Amoxicilline', 'ANTIBIOTIQUE', 'Antibiotique à large spectre', '1g', true, NOW(), NOW()),
    (gen_random_uuid(), 'Azithromycine 250mg', 'Azithromycine', 'ANTIBIOTIQUE', 'Antibiotique de la famille des macrolides', '250mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Ciprofloxacine 500mg', 'Ciprofloxacine', 'ANTIBIOTIQUE', 'Antibiotique de la famille des fluoroquinolones', '500mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Augmentin 1g', 'Amoxicilline + Acide clavulanique', 'ANTIBIOTIQUE', 'Association antibiotique à large spectre', '1g', true, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Antipaludéens
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Artéméther + Luméfantrine', 'Artéméther + Luméfantrine', 'ANTIPALUDEEN', 'Traitement du paludisme non compliqué', '20mg/120mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Quinine 500mg', 'Quinine', 'ANTIPALUDEEN', 'Traitement du paludisme sévère', '500mg', true, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Antihypertenseurs
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Amlodipine 5mg', 'Amlodipine', 'ANTIHYPERTENSEUR', 'Inhibiteur calcique pour hypertension', '5mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Amlodipine 10mg', 'Amlodipine', 'ANTIHYPERTENSEUR', 'Traitement de l''hypertension artérielle', '10mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Enalapril 10mg', 'Enalapril', 'ANTIHYPERTENSEUR', 'Inhibiteur de l''enzyme de conversion', '10mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Losartan 50mg', 'Losartan', 'ANTIHYPERTENSEUR', 'Antagoniste des récepteurs de l''angiotensine II', '50mg', true, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Antidiabétiques
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Metformine 500mg', 'Metformine', 'ANTIDIABETIQUE', 'Antidiabétique oral pour diabète de type 2', '500mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Metformine 850mg', 'Metformine', 'ANTIDIABETIQUE', 'Biguanide pour le traitement du diabète', '850mg', true, NOW(), NOW()),
    (gen_random_uuid(), 'Glibenclamide 5mg', 'Glibenclamide', 'ANTIDIABETIQUE', 'Sulfamide hypoglycémiant', '5mg', true, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Vitamines et Suppléments
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Vitamine C 1000mg', 'Acide ascorbique', 'VITAMINE', 'Supplément en vitamine C', '1000mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Vitamine D3 1000 UI', 'Cholécalciférol', 'VITAMINE', 'Supplément en vitamine D', '1000 UI', false, NOW(), NOW()),
    (gen_random_uuid(), 'Fer 80mg', 'Sulfate ferreux', 'VITAMINE', 'Supplément en fer pour anémie', '80mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Calcium 500mg', 'Carbonate de calcium', 'VITAMINE', 'Supplément calcique', '500mg', false, NOW(), NOW()),
    (gen_random_uuid(), 'Multivitamines', 'Complexe multivitaminé', 'VITAMINE', 'Supplément vitaminique complet', 'Comprimé', false, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;

-- Autres médicaments courants
INSERT INTO medications (id, name, generic_name, therapeutic_class, description, dosage, requires_prescription, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Sirop contre la toux', 'Dextrométhorphane', 'AUTRE', 'Antitussif pour toux sèche', '15mg/5ml', false, NOW(), NOW()),
    (gen_random_uuid(), 'Collyre', 'Larmes artificielles', 'AUTRE', 'Hydratation oculaire', 'Solution', false, NOW(), NOW()),
    (gen_random_uuid(), 'Pommade cicatrisante', 'Allantoïne', 'AUTRE', 'Favorise la cicatrisation', 'Pommade', false, NOW(), NOW())
ON CONFLICT (name, dosage) DO NOTHING;
