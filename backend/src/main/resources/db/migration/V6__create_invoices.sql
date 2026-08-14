CREATE SEQUENCE invoice_number_seq START WITH 1;

CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number  BIGINT NOT NULL UNIQUE DEFAULT nextval('invoice_number_seq'),
    party_id        UUID NOT NULL REFERENCES parties (id),
    issue_date      DATE NOT NULL,
    due_date        DATE NOT NULL,
    currency        VARCHAR(3) NOT NULL CHECK (currency IN ('HNL', 'USD')),
    exchange_rate   NUMERIC(12, 6) NOT NULL DEFAULT 1,
    subtotal        NUMERIC(19, 4) NOT NULL,
    tax_amount      NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total           NUMERIC(19, 4) NOT NULL,
    amount_in_base  NUMERIC(19, 4) NOT NULL,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    journal_entry_id UUID REFERENCES journal_entries (id),
    notes           VARCHAR(1000),
    created_by      UUID NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE invoice_lines (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id  UUID NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity    NUMERIC(19, 4) NOT NULL,
    unit_price  NUMERIC(19, 4) NOT NULL,
    tax_rate    NUMERIC(5, 2) NOT NULL DEFAULT 15.00,
    line_total  NUMERIC(19, 4) NOT NULL,
    account_id  UUID NOT NULL REFERENCES accounts (id)
);

CREATE INDEX idx_invoices_party_id ON invoices (party_id);
CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoice_lines_invoice_id ON invoice_lines (invoice_id);
