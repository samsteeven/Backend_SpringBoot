-- ========================================
-- Flyway Migration V7 - Create payouts table
-- File: src/main/resources/db/migrations/V7__create_payouts_table.sql
-- ========================================

-- Create payouts table
CREATE TABLE payouts (
    id UUID PRIMARY KEY,
    pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    transaction_reference VARCHAR(255) NOT NULL,
    method VARCHAR(50) NOT NULL CHECK (method IN ('MTN_MOMO_PAYOUT', 'CASH_PAYOUT')),
    notes TEXT,
    processed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_payouts_pharmacy_id ON payouts(pharmacy_id);
CREATE INDEX idx_payouts_transaction_reference ON payouts(transaction_reference);
CREATE INDEX idx_payouts_method ON payouts(method);
CREATE INDEX idx_payouts_processed_at ON payouts(processed_at);

-- ========================================
-- FIN DE LA MIGRATION V7
-- ========================================