package com.digitalbank.payments.beneficiary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IbanValidatorTest {
    @Test void acceptsValidIbans() {
        assertTrue(IbanValidator.isValid("FR76 3000 6000 0112 3456 7890 189"));
        assertTrue(IbanValidator.isValid("DE89370400440532013000"));
    }
    @Test void rejectsBadChecksum() {
        assertFalse(IbanValidator.isValid("DE89370400440532013001"));
    }
    @Test void rejectsGarbage() {
        assertFalse(IbanValidator.isValid("hello"));
    }
}