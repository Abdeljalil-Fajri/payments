package com.digitalbank.payments.transfer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id private UUID id;
    private UUID customerId;
    private String iban;
    private BigDecimal balance;
    private String currency;

    protected Account() {}

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getIban() { return iban; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }

    public void debit(BigDecimal amount) { balance = balance.subtract(amount); }
    public void credit(BigDecimal amount) { balance = balance.add(amount); }
}