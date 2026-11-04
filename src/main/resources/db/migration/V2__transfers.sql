ALTER TABLE accounts ALTER COLUMN currency TYPE VARCHAR(3);

CREATE TABLE transfers (
                           id               UUID PRIMARY KEY,
                           customer_id      UUID NOT NULL,
                           idempotency_key  VARCHAR(64) NOT NULL,
                           source_iban      VARCHAR(34) NOT NULL,
                           destination_iban VARCHAR(34) NOT NULL,
                           amount           NUMERIC(19,2) NOT NULL CHECK (amount > 0),
                           currency         VARCHAR(3) NOT NULL,
                           status           VARCHAR(20) NOT NULL,
                           reference        VARCHAR(140),
                           created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                           CHECK (source_iban <> destination_iban),
                           UNIQUE (customer_id, idempotency_key)
);