package com.digitalbank.payments.transfer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
    @Id private UUID id = UUID.randomUUID();
    private UUID customerId;
    private String idempotencyKey;
    private String sourceIban;
    private String destinationIban;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private TransferStatus status;
    @Enumerated(EnumType.STRING)
    private TransferType type;
    private String reference;
    private String failureReason;
    private Instant createdAt = Instant.now();

    protected Transfer() {}

    // Used by internal transfers (DBP-22): type defaults to INTERNAL.
    public Transfer(UUID customerId, String idempotencyKey, String sourceIban,
                    String destinationIban, BigDecimal amount, String currency,
                    TransferStatus status, String reference) {
        this(customerId, idempotencyKey, sourceIban, destinationIban, amount,
                currency, status, reference, TransferType.INTERNAL);
    }

    public Transfer(UUID customerId, String idempotencyKey, String sourceIban,
                    String destinationIban, BigDecimal amount, String currency,
                    TransferStatus status, String reference, TransferType type) {
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.sourceIban = sourceIban;
        this.destinationIban = destinationIban;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reference = reference;
        this.type = type;
    }

    // Only a PENDING transfer can change state, and only once.
    public void markCompleted() {
        requirePending();
        this.status = TransferStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        requirePending();
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
    }

    private void requirePending() {
        if (status != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer is not PENDING");
        }
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getSourceIban() { return sourceIban; }
    public String getDestinationIban() { return destinationIban; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public TransferType getType() { return type; }
    public String getReference() { return reference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}