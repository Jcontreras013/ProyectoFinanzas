CREATE SEQUENCE expense_number_seq START WITH 1;

CREATE TABLE expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_number  BIGINT NOT NULL UNIQUE DEFAULT nextval('expense_number_seq'),
    party_id        UUID REFERENCES parties (id),
    expense_date    DATE NOT NULL,
    currency        VARCHAR(3) NOT NULL CHECK (currency IN ('HNL', 'USD')),
    exchange_rate   NUMERIC(12, 6) NOT NULL DEFAULT 1,
    account_id      UUID NOT NULL REFERENCES accounts (id),
    description     VARCHAR(500) NOT NULL,
    payment_method  VARCHAR(10) NOT NULL CHECK (payment_method IN ('CASH', 'BANK', 'CREDIT')),
    amount          NUMERIC(19, 4) NOT NULL,
    amount_in_base  NUMERIC(19, 4) NOT NULL,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('POSTED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    journal_entry_id UUID REFERENCES journal_entries (id),
    created_by      UUID NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_expenses_party_id ON expenses (party_id);
CREATE INDEX idx_expenses_status ON expenses (status);
