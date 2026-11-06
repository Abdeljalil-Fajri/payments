package com.digitalbank.payments.transfer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// SIMULATION: in production this would be replaced by a real SEPA clearing integration.
@Component
public class SepaSettlementJob {
    private final TransferRepository transfers;

    public SepaSettlementJob(TransferRepository transfers) { this.transfers = transfers; }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void settle() {
        List<Transfer> pending = transfers.findByTypeAndStatus(TransferType.SEPA, TransferStatus.PENDING);
        pending.forEach(Transfer::markCompleted);
    }
}