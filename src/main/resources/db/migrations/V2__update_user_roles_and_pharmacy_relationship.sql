-- ========================================
-- Flyway Migration V2 - User Role Refactoring
-- File: src/main/resources/db/migrations/V2__update_user_roles_and_pharmacy_relationship.sql
-- ========================================

-- Step 1: Add pharmacy_id column to users table
ALTER TABLE users
ADD COLUMN pharmacy_id UUID;

-- Step 2: Add foreign key constraint
ALTER TABLE users
ADD CONSTRAINT fk_users_pharmacy
FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE SET NULL;

-- Step 3: Create index for pharmacy_id
CREATE INDEX idx_users_pharmacy_id ON users(pharmacy_id);

-- Step 4: Update existing ADMIN roles to SUPER_ADMIN
UPDATE users
SET role = 'SUPER_ADMIN'
WHERE role = 'ADMIN';

-- Step 5: Update existing PHARMACIST roles to PHARMACY_ADMIN
-- Users with a pharmacy become PHARMACY_ADMIN
UPDATE users u
SET role = 'PHARMACY_ADMIN'
WHERE role = 'PHARMACIST'
AND EXISTS (
    SELECT 1 FROM pharmacies p WHERE p.user_id = u.id
);

-- Step 6: Users who are PHARMACIST but don't have a pharmacy yet remain PHARMACY_ADMIN
-- (they can create one later)
UPDATE users
SET role = 'PHARMACY_ADMIN'
WHERE role = 'PHARMACIST';

-- Step 7: Update the CHECK constraint on role column to include new roles
ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
ADD CONSTRAINT users_role_check
CHECK (role IN ('SUPER_ADMIN', 'PHARMACY_ADMIN', 'PHARMACY_EMPLOYEE', 'PATIENT', 'DELIVERY'));

-- ========================================
-- END OF MIGRATION V2
-- ========================================
