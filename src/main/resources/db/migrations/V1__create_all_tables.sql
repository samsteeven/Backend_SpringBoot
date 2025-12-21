-- ========================================
-- Flyway Migration V1 - EasyPharma Database
-- File: src/main/resources/db/migration/V1__create_all_tables.sql
-- ========================================

-- Table: users
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20) NOT NULL UNIQUE,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'PHARMACY_ADMIN', 'PHARMACY_EMPLOYEE', 'PATIENT', 'DELIVERY')),
                       address TEXT,
                       city VARCHAR(100),
                       latitude DECIMAL(10, 8) CHECK (latitude BETWEEN -90 AND 90),
                       longitude DECIMAL(11, 8) CHECK (longitude BETWEEN -180 AND 180),
                       pharmacy_id UUID,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_pharmacy_id ON users(pharmacy_id);

-- Table: refresh_tokens
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Table: pharmacies
CREATE TABLE pharmacies (
                            id UUID PRIMARY KEY,
                            user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                            name VARCHAR(200) NOT NULL,
                            license_number VARCHAR(50) NOT NULL UNIQUE,
                            address TEXT NOT NULL,
                            city VARCHAR(100) NOT NULL,
                            phone VARCHAR(20) NOT NULL,
                            latitude DECIMAL(10, 8) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
                            longitude DECIMAL(11, 8) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
                            description TEXT,
                            opening_hours VARCHAR(255),
                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
                            license_document_url VARCHAR(500) NOT NULL,
                            validated_at TIMESTAMP,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT chk_pharmacy_status CHECK (status != 'APPROVED' OR validated_at IS NOT NULL)
    );

CREATE INDEX idx_pharmacies_city ON pharmacies(city);
CREATE INDEX idx_pharmacies_status ON pharmacies(status);
CREATE INDEX idx_pharmacies_location ON pharmacies(latitude, longitude);

-- Add foreign key constraint for users.pharmacy_id
ALTER TABLE users
    ADD CONSTRAINT fk_users_pharmacy
        FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE SET NULL;

-- Table: medications
CREATE TABLE medications (
                             id UUID PRIMARY KEY,
                             name VARCHAR(200) NOT NULL,
                             generic_name VARCHAR(200),
                             therapeutic_class VARCHAR(50) NOT NULL CHECK (therapeutic_class IN ('ANTALGIQUE', 'ANTIBIOTIQUE', 'ANTIPALUDEEN', 'ANTIHYPERTENSEUR', 'ANTIINFLAMMATOIRE', 'ANTIDIABETIQUE', 'VITAMINE', 'AUTRE')),
                             description TEXT,
                             dosage VARCHAR(100),
                             photo_url VARCHAR(500),
                             notice_pdf_url VARCHAR(500),
                             requires_prescription BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT unique_medication_name_dosage UNIQUE (name, dosage)
);

CREATE INDEX idx_medications_name ON medications(name);
CREATE INDEX idx_medications_therapeutic_class ON medications(therapeutic_class);

-- Table: pharmacy_medications
CREATE TABLE pharmacy_medications (
                                      id UUID PRIMARY KEY,
                                      pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
                                      medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
                                      price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
                                      stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                                      is_available BOOLEAN NOT NULL DEFAULT TRUE,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT unique_pharmacy_medication UNIQUE (pharmacy_id, medication_id)
);

CREATE INDEX idx_pharmacy_medications_pharmacy_id ON pharmacy_medications(pharmacy_id);
CREATE INDEX idx_pharmacy_medications_medication_id ON pharmacy_medications(medication_id);
CREATE INDEX idx_pharmacy_medications_available ON pharmacy_medications(is_available) WHERE is_available = TRUE;

-- Table: orders
CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        order_number VARCHAR(20) NOT NULL UNIQUE,
                        patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
                        total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'CONFIRMED', 'PREPARING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
                        delivery_address TEXT NOT NULL,
                        delivery_city VARCHAR(100) NOT NULL,
                        delivery_phone VARCHAR(20) NOT NULL,
                        delivery_latitude DECIMAL(10, 8) CHECK (delivery_latitude BETWEEN -90 AND 90),
                        delivery_longitude DECIMAL(11, 8) CHECK (delivery_longitude BETWEEN -180 AND 180),
                        notes TEXT,
                        confirmed_at TIMESTAMP,
                        delivered_at TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_order_dates CHECK (delivered_at IS NULL OR confirmed_at IS NULL OR delivered_at >= confirmed_at),
                        CONSTRAINT chk_order_delivered CHECK (status != 'DELIVERED' OR delivered_at IS NOT NULL)
    );

CREATE INDEX idx_orders_patient_id ON orders(patient_id);
CREATE INDEX idx_orders_pharmacy_id ON orders(pharmacy_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_order_number ON orders(order_number);

-- Table: order_items
CREATE TABLE order_items (
                             id UUID PRIMARY KEY,
                             order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE RESTRICT,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
                             subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal > 0),
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT unique_order_medication UNIQUE (order_id, medication_id),
                             CONSTRAINT chk_order_item_subtotal CHECK (subtotal = quantity * unit_price)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_medication_id ON order_items(medication_id);

-- Table: payments
CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
                          payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('MTN_MOMO', 'ORANGE_MONEY', 'CASH')),
                          phone_number VARCHAR(20),
                          amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
                          transaction_id VARCHAR(100) UNIQUE,
                          receipt_url VARCHAR(500),
                          paid_at TIMESTAMP,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT chk_payment_mobile_money CHECK (payment_method = 'CASH' OR (payment_method IN ('MTN_MOMO', 'ORANGE_MONEY') AND phone_number IS NOT NULL)),
                          CONSTRAINT chk_payment_success CHECK (status != 'SUCCESS' OR (paid_at IS NOT NULL AND transaction_id IS NOT NULL)),
    CONSTRAINT chk_payment_dates CHECK (paid_at IS NULL OR paid_at >= created_at)
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);

-- Table: deliveries
CREATE TABLE deliveries (
                            id UUID PRIMARY KEY,
                            order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
                            delivery_person_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                            status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED')),
                            current_latitude DECIMAL(10, 8) CHECK (current_latitude BETWEEN -90 AND 90),
                            current_longitude DECIMAL(11, 8) CHECK (current_longitude BETWEEN -180 AND 180),
                            photo_proof_url VARCHAR(500),
                            assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            picked_up_at TIMESTAMP,
                            delivered_at TIMESTAMP,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT chk_delivery_coordinates CHECK ((current_latitude IS NULL AND current_longitude IS NULL) OR (current_latitude IS NOT NULL AND current_longitude IS NOT NULL)),
                            CONSTRAINT chk_delivery_dates CHECK ((picked_up_at IS NULL OR picked_up_at >= assigned_at) AND (delivered_at IS NULL OR picked_up_at IS NULL OR delivered_at >= picked_up_at)),
                            CONSTRAINT chk_delivery_status CHECK (status != 'DELIVERED' OR (delivered_at IS NOT NULL AND photo_proof_url IS NOT NULL))
    );

CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_delivery_person_id ON deliveries(delivery_person_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);

-- Table: reviews
CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
                         order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
                         rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT CHECK (LENGTH(comment) <= 1000),
                         status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_patient_id ON reviews(patient_id);
CREATE INDEX idx_reviews_pharmacy_id ON reviews(pharmacy_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Table: notifications
CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               title VARCHAR(200) NOT NULL,
                               message TEXT NOT NULL,
                               type VARCHAR(20) NOT NULL CHECK (type IN ('ORDER', 'PAYMENT', 'DELIVERY', 'SYSTEM')),
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               read_at TIMESTAMP,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT chk_notification_read CHECK (is_read = FALSE OR read_at IS NOT NULL),
                               CONSTRAINT chk_notification_dates CHECK (read_at IS NULL OR read_at >= created_at)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- ========================================
-- TRIGGERS pour updated_at automatique
-- ========================================

-- Fonction trigger générique
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Appliquer le trigger sur toutes les tables avec updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_pharmacies_updated_at BEFORE UPDATE ON pharmacies FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_medications_updated_at BEFORE UPDATE ON medications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_pharmacy_medications_updated_at BEFORE UPDATE ON pharmacy_medications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_deliveries_updated_at BEFORE UPDATE ON deliveries FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger pour mettre à jour is_available si stock = 0
CREATE OR REPLACE FUNCTION update_medication_availability()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.stock_quantity = 0 THEN
        NEW.is_available = FALSE;
    ELSIF NEW.stock_quantity > 0 THEN
        NEW.is_available = TRUE;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_pharmacy_medications_availability
    BEFORE INSERT OR UPDATE ON pharmacy_medications
                         FOR EACH ROW EXECUTE FUNCTION update_medication_availability();

-- ========================================
-- DONNÉES DE TEST (Optionnel - pour dev)
-- ========================================

-- Insertion d'un admin par défaut
INSERT INTO users (id, email, password, first_name, last_name, phone, role, is_active, is_verified)
VALUES (
           gen_random_uuid(),
           'admin@easypharma.cm',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
           'Admin',
           'EasyPharma',
           '+237600000000',
           'SUPER_ADMIN',
           TRUE,
           TRUE
       );

-- ========================================
-- FIN DE LA MIGRATION V1
-- ========================================