package com.digitalbank.payments.transfer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SepaSettlementService {
    // SIMULATION: IBANs the "receiving bank" rejects. Replaced by real clearing responses in production.
    private static final Set<String> REJECTED_IBANS = Set.of("GB29NWBK60161331926819");

    private final TransferRepository transfers;
    private final AccountRepository accounts;

    public SepaSettlementService(TransferRepository transfers, AccountRepository accounts) {
        this.transfers = transfers;
        this.accounts = accounts;
    }

    // One transaction per transfer: a failure here rolls back only this transfer.
    @Transactional
    public void settleOne(UUID transferId) {
        Transfer t = transfers.lockById(transferId).orElse(null);
        if (t == null || t.getStatus() != TransferStatus.PENDING) {
            return; // already handled: never refund or settle twice
        }

        if (REJECTED_IBANS.contains(t.getDestinationIban())) {
            List<Account> locked = accounts.lockByIbans(Set.of(t.getSourceIban()));
            if (locked.isEmpty()) {
                throw new IllegalStateException("Source account missing, refund impossible");
            }
            locked.get(0).credit(t.getAmount());   // refund
            t.markFailed("REJECTED_BY_BANK");      // same transaction as the refund
        } else {
            t.markCompleted();
        }
    }
}