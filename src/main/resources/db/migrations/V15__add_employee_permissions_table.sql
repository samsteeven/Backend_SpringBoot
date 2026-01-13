CREATE TABLE IF NOT EXISTS employee_permissions (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL UNIQUE,
    can_prepare_orders BOOLEAN NOT NULL DEFAULT TRUE,
    can_assign_deliveries BOOLEAN NOT NULL DEFAULT TRUE,
    can_view_statistics BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_inventory BOOLEAN NOT NULL DEFAULT FALSE,
    can_view_customer_info BOOLEAN NOT NULL DEFAULT TRUE,
    can_process_payments BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_modified_by UUID,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE
);
