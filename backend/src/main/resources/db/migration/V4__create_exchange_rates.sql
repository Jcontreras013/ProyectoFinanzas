-- Tasa de cambio USD -> HNL, mantenida manualmente por ADMIN/ACCOUNTANT.
CREATE TABLE exchange_rates (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rate_date  DATE NOT NULL UNIQUE,
    rate       NUMERIC(12, 6) NOT NULL CHECK (rate > 0),
    created_by UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
