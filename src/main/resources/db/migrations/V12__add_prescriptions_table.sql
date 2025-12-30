-- Migration V12: Table des ordonnances (Prescriptions)
-- Description: Stocker les photos d'ordonnances envoyées par les patients.

CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id), -- Le patient
    order_id UUID REFERENCES orders(id), -- Peut être lié à une commande plus tard
    photo_url TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    pharmacist_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prescriptions_user ON prescriptions(user_id);
CREATE INDEX idx_prescriptions_order ON prescriptions(order_id);
