CREATE TABLE accounts (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(20) NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(20) NOT NULL CHECK (type IN ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE')),
    parent_id      UUID REFERENCES accounts (id),
    allows_posting BOOLEAN NOT NULL DEFAULT TRUE,
    system_role    VARCHAR(30) CHECK (system_role IN
                    ('ACCOUNTS_RECEIVABLE', 'ACCOUNTS_PAYABLE', 'SALES_REVENUE_DEFAULT', 'TAX_PAYABLE',
                     'CASH_HNL', 'CASH_USD')),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_parent_id ON accounts (parent_id);
CREATE INDEX idx_accounts_type ON accounts (type);
-- Cada rol contable especial (CxC, CxP, caja, etc.) apunta a una única cuenta.
CREATE UNIQUE INDEX idx_accounts_system_role ON accounts (system_role) WHERE system_role IS NOT NULL;
