CREATE SEQUENCE payment_number_seq START WITH 1;

-- Un pago aplica a una factura (cobro) O a un gasto (pago a proveedor), nunca ambos.
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number  BIGINT NOT NULL UNIQUE DEFAULT nextval('payment_number_seq'),
    invoice_id      UUID REFERENCES invoices (id),
    expense_id      UUID REFERENCES expenses (id),
    amount          NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3) NOT NULL CHECK (currency IN ('HNL', 'USD')),
    exchange_rate   NUMERIC(12, 6) NOT NULL DEFAULT 1,
    amount_in_base  NUMERIC(19, 4) NOT NULL,
    payment_date    DATE NOT NULL,
    method          VARCHAR(10) NOT NULL CHECK (method IN ('CASH', 'BANK')),
    journal_entry_id UUID REFERENCES journal_entries (id),
    created_by      UUID NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_payment_target CHECK (
        (invoice_id IS NOT NULL AND expense_id IS NULL) OR
        (invoice_id IS NULL AND expense_id IS NOT NULL)
    )
);

CREATE INDEX idx_payments_invoice_id ON payments (invoice_id);
CREATE INDEX idx_payments_expense_id ON payments (expense_id);
