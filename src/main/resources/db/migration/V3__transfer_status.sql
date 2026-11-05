ALTER TABLE transfers
    ADD CONSTRAINT transfers_status_check
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'));

CREATE INDEX idx_transfers_customer_created
    ON transfers (customer_id, created_at DESC);