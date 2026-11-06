package com.digitalbank.payments.transfer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransferDtos {
    private TransferDtos() {}

    public record Request(
            @NotBlank @Size(max = 42) String sourceIban,
            @NotBlank @Size(max = 42) String destinationIban,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @Size(max = 140) String reference) {}

    public record Response(UUID id, TransferStatus status, TransferType type,
                           String sourceIban, String destinationIban,
                           BigDecimal amount, String currency, String reference,
                           String failureReason, Instant createdAt) {
        static Response from(Transfer t) {
            return new Response(t.getId(), t.getStatus(), t.getType(),
                    t.getSourceIban(), t.getDestinationIban(), t.getAmount(),
                    t.getCurrency(), t.getReference(), t.getFailureReason(),
                    t.getCreatedAt());
        }
    }
}