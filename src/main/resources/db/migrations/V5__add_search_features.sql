-- Add symptoms column to medications table
ALTER TABLE medications ADD COLUMN symptoms TEXT;

-- Add rating columns to pharmacies table
ALTER TABLE pharmacies ADD COLUMN average_rating DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE pharmacies ADD COLUMN rating_count INTEGER DEFAULT 0;
