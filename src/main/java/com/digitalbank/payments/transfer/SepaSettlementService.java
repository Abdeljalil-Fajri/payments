package com.digitalbank.payments.transfer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SepaSettlementService {
    private final TransferRepository transfers;
    private final AccountRepository accounts;
    private final SimulatedBank bank;

    public SepaSettlementService(TransferRepository transfers, AccountRepository accounts,
                                 SimulatedBank bank) {
        this.transfers = transfers;
        this.accounts = accounts;
        this.bank = bank;
    }

    // One transaction per transfer: a failure here rolls back only this transfer.
    @Transactional
    public void settleOne(UUID transferId) {
        Transfer t = transfers.lockById(transferId).orElse(null);
        if (t == null || t.getStatus() != TransferStatus.PENDING) {
            return; // already handled: never refund or settle twice
        }

        if (bank.isRejected(t.getDestinationIban())) {
            List<Account> locked = accounts.lockByIbans(Set.of(t.getSourceIban()));
            if (locked.isEmpty()) {
                throw new IllegalStateException("Source account missing, refund impossible");
            }
            locked.get(0).credit(t.getAmount());
            t.markFailed("REJECTED_BY_BANK");
        } else {
            t.markCompleted();
        }
    }
}