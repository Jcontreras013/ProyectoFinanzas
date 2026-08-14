CREATE TABLE audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_name VARCHAR(100) NOT NULL,
    entity_id   UUID NOT NULL,
    action      VARCHAR(10) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    user_id     UUID NOT NULL REFERENCES users (id),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    old_value   JSONB,
    new_value   JSONB
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_name, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log (user_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at);
