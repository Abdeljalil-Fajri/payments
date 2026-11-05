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
    private String reference;
    private Instant createdAt = Instant.now();

    protected Transfer() {}

    public Transfer(UUID customerId, String idempotencyKey, String sourceIban,
                    String destinationIban, BigDecimal amount, String currency,
                    TransferStatus status, String reference) {
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.sourceIban = sourceIban;
        this.destinationIban = destinationIban;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reference = reference;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getSourceIban() { return sourceIban; }
    public String getDestinationIban() { return destinationIban; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}