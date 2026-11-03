CREATE TABLE accounts (
                          id          UUID PRIMARY KEY,
                          customer_id UUID NOT NULL,
                          iban        VARCHAR(34) NOT NULL UNIQUE,
                          balance     NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
                          currency    CHAR(3) NOT NULL DEFAULT 'EUR',
                          created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE beneficiaries (
                               id          UUID PRIMARY KEY,
                               customer_id UUID NOT NULL,
                               name        VARCHAR(100) NOT NULL,
                               iban        VARCHAR(34) NOT NULL,
                               bic         VARCHAR(11),
                               created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                               UNIQUE (customer_id, iban)
);