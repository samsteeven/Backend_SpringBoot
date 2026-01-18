-- Fix users_role_check constraint to ensure all UserRoles are included
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
    CHECK (role IN ('SUPER_ADMIN', 'PHARMACY_ADMIN', 'PHARMACY_EMPLOYEE', 'PATIENT', 'DELIVERY'));
