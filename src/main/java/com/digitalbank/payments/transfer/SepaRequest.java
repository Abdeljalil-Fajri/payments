package com.digitalbank.payments.transfer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record SepaRequest(
        @NotBlank @Size(max = 42) String sourceIban,
        @NotNull UUID beneficiaryId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Size(max = 140) String reference) {}