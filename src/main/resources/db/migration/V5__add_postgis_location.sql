-- Enable PostGIS extension (if not already enabled)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add location column to pharmacies table
ALTER TABLE pharmacies ADD COLUMN location geometry(Point, 4326);

-- Populate location column from existing latitude and longitude
UPDATE pharmacies 
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- Create spatial index for performance
CREATE INDEX idx_pharmacies_location ON pharmacies USING GIST (location);
