CREATE SEQUENCE journal_entry_number_seq START WITH 1;

CREATE TABLE journal_entries (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_number   BIGINT NOT NULL UNIQUE DEFAULT nextval('journal_entry_number_seq'),
    entry_date     DATE NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    source_type    VARCHAR(20) NOT NULL CHECK (source_type IN ('MANUAL', 'INVOICE', 'EXPENSE', 'PAYMENT', 'REVERSAL')),
    source_id      UUID,
    reversal_of_id UUID REFERENCES journal_entries (id),
    created_by     UUID NOT NULL REFERENCES users (id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_je_entry_date ON journal_entries (entry_date);
CREATE INDEX idx_je_source ON journal_entries (source_type, source_id);

-- Cada línea es débito XOR crédito (nunca ambos, nunca ninguno) — la partida
-- doble se hace explícita a nivel de esquema, no solo en código de aplicación.
CREATE TABLE journal_entry_lines (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id  UUID NOT NULL REFERENCES journal_entries (id) ON DELETE CASCADE,
    line_number       INT NOT NULL,
    account_id        UUID NOT NULL REFERENCES accounts (id),
    party_id          UUID REFERENCES parties (id),
    debit             NUMERIC(19, 4) NOT NULL DEFAULT 0,
    credit            NUMERIC(19, 4) NOT NULL DEFAULT 0,
    description       VARCHAR(500),
    CONSTRAINT chk_jel_debit_xor_credit CHECK (
        (debit > 0 AND credit = 0) OR (credit > 0 AND debit = 0)
    )
);

CREATE INDEX idx_jel_journal_entry_id ON journal_entry_lines (journal_entry_id);
CREATE INDEX idx_jel_account_id ON journal_entry_lines (account_id);
CREATE INDEX idx_jel_party_id ON journal_entry_lines (party_id);
