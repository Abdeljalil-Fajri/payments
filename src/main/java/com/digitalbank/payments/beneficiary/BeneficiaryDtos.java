package com.digitalbank.payments.beneficiary;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class BeneficiaryDtos {
    private BeneficiaryDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 42) String iban,
            @Size(max = 11) String bic) {}

    public record Response(UUID id, String name, String iban, String bic) {
        static Response from(Beneficiary b) {
            return new Response(b.getId(), b.getName(), b.getIban(), b.getBic());
        }
    }
}