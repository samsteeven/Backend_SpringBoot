-- Add delivery_fee column to orders table
ALTER TABLE orders ADD COLUMN delivery_fee DECIMAL(10, 2);

-- Update existing orders with default fee (e.g., 0.00)
UPDATE orders SET delivery_fee = 0.00 WHERE delivery_fee IS NULL;
