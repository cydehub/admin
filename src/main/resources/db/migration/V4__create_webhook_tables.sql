CREATE TABLE webhook_endpoints (
    id UUID PRIMARY KEY,
    url VARCHAR(300) NOT NULL UNIQUE,
    secret VARCHAR(200),
    event_types VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_webhook_endpoints_active ON webhook_endpoints (active);

CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    endpoint_id UUID NOT NULL REFERENCES webhook_endpoints(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    resource_type VARCHAR(60) NOT NULL,
    resource_id UUID,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_response_code INTEGER,
    last_error VARCHAR(500),
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    last_attempt_at TIMESTAMPTZ
);

CREATE INDEX idx_webhook_deliveries_endpoint ON webhook_deliveries (endpoint_id);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries (status);
CREATE INDEX idx_webhook_deliveries_created ON webhook_deliveries (created_at);
