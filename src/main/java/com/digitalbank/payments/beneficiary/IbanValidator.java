package com.digitalbank.payments.beneficiary;

import java.math.BigInteger;

public final class IbanValidator {
    private IbanValidator() {}

    public static String normalize(String iban) {
        return iban.replaceAll("\\s+", "").toUpperCase();
    }

    public static boolean isValid(String raw) {
        String iban = normalize(raw);
        if (!iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")) return false;
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            digits.append(Character.digit(c, 36)); // A=10 ... Z=35
        }
        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}