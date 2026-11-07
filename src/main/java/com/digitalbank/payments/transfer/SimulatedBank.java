package com.digitalbank.payments.transfer;

import org.springframework.stereotype.Component;
import java.util.Set;

// SIMULATION: replaced by real bank/clearing responses in production.
@Component
public class SimulatedBank {
    private static final Set<String> REJECTED_IBANS = Set.of("GB29NWBK60161331926819");

    public boolean isRejected(String iban) {
        return REJECTED_IBANS.contains(iban);
    }
}