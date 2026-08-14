CREATE TABLE parties (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type       VARCHAR(10) NOT NULL CHECK (type IN ('CUSTOMER', 'VENDOR', 'BOTH')),
    name       VARCHAR(255) NOT NULL,
    rtn        VARCHAR(20),
    email      VARCHAR(255),
    phone      VARCHAR(30),
    address    VARCHAR(500),
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_parties_type ON parties (type);
