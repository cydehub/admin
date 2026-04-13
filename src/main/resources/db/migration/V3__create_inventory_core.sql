CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_warehouses_code ON warehouses (code);

CREATE TABLE buildings (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_buildings_warehouse_code UNIQUE (warehouse_id, code)
);

CREATE INDEX idx_buildings_warehouse ON buildings (warehouse_id);

CREATE TABLE aisles (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_aisles_building_code UNIQUE (building_id, code)
);

CREATE INDEX idx_aisles_building ON aisles (building_id);

CREATE TABLE shelves (
    id UUID PRIMARY KEY,
    aisle_id UUID NOT NULL REFERENCES aisles(id) ON DELETE CASCADE,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_shelves_aisle_code UNIQUE (aisle_id, code)
);

CREATE INDEX idx_shelves_aisle ON shelves (aisle_id);

CREATE TABLE bins (
    id UUID PRIMARY KEY,
    shelf_id UUID NOT NULL REFERENCES shelves(id) ON DELETE CASCADE,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160),
    barcode VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_bins_shelf_code UNIQUE (shelf_id, code)
);

CREATE INDEX idx_bins_shelf ON bins (shelf_id);
CREATE INDEX idx_bins_barcode ON bins (barcode);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    unit_of_measure VARCHAR(40),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_products_sku ON products (sku);

CREATE TABLE batches (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    batch_code VARCHAR(120) NOT NULL,
    supplier_reference VARCHAR(160),
    manufacture_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_batches_product_code UNIQUE (product_id, batch_code)
);

CREATE INDEX idx_batches_product ON batches (product_id);
CREATE INDEX idx_batches_expiry ON batches (expiry_date);

CREATE TABLE inventory_balances (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    batch_id UUID NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
    bin_id UUID NOT NULL REFERENCES bins(id) ON DELETE CASCADE,
    quantity_on_hand NUMERIC(19,3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(19,3) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_inventory_balances UNIQUE (product_id, batch_id, bin_id),
    CONSTRAINT chk_inventory_balances_non_negative CHECK (quantity_on_hand >= 0 AND reserved_quantity >= 0)
);

CREATE INDEX idx_inventory_balances_product ON inventory_balances (product_id);
CREATE INDEX idx_inventory_balances_batch ON inventory_balances (batch_id);
CREATE INDEX idx_inventory_balances_bin ON inventory_balances (bin_id);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    batch_id UUID NOT NULL REFERENCES batches(id) ON DELETE RESTRICT,
    from_bin_id UUID REFERENCES bins(id) ON DELETE SET NULL,
    to_bin_id UUID REFERENCES bins(id) ON DELETE SET NULL,
    movement_type VARCHAR(24) NOT NULL,
    quantity NUMERIC(19,3) NOT NULL,
    reference VARCHAR(120),
    reason_code VARCHAR(80),
    performed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_stock_movements_product ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_batch ON stock_movements (batch_id);
CREATE INDEX idx_stock_movements_user ON stock_movements (performed_by);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);

CREATE TABLE reorder_rules (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    reorder_point NUMERIC(19,3) NOT NULL,
    reorder_quantity NUMERIC(19,3) NOT NULL,
    safety_stock NUMERIC(19,3) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_reorder_rules_product UNIQUE (product_id)
);

CREATE INDEX idx_reorder_rules_product ON reorder_rules (product_id);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID,
    quantity_delta NUMERIC(19,3),
    reason_code VARCHAR(80),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action_type);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
