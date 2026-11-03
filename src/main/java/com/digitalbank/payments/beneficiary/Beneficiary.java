package com.digitalbank.payments.beneficiary;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    @Id private UUID id = UUID.randomUUID();
    private UUID customerId;
    private String name;
    private String iban;
    private String bic;
    private Instant createdAt = Instant.now();

    protected Beneficiary() {}

    public Beneficiary(UUID customerId, String name, String iban, String bic) {
        this.customerId = customerId;
        this.name = name;
        this.iban = iban;
        this.bic = bic;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getIban() { return iban; }
    public String getBic() { return bic; }
    public Instant getCreatedAt() { return createdAt; }
}