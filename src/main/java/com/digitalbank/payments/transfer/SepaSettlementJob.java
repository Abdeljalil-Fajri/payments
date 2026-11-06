package com.digitalbank.payments.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// SIMULATION: in production this would be replaced by a real SEPA clearing integration.
@Component
public class SepaSettlementJob {
    private static final Logger log = LoggerFactory.getLogger(SepaSettlementJob.class);

    private final TransferRepository transfers;
    private final SepaSettlementService settlement;

    public SepaSettlementJob(TransferRepository transfers, SepaSettlementService settlement) {
        this.transfers = transfers;
        this.settlement = settlement;
    }

    @Scheduled(fixedDelay = 30000)
    public void settle() {
        for (Transfer t : transfers.findByTypeAndStatus(TransferType.SEPA, TransferStatus.PENDING)) {
            try {
                settlement.settleOne(t.getId());
            } catch (Exception e) {
                // The transfer stays PENDING and is retried at the next run. No IBAN in logs.
                log.error("Settlement failed for transfer {}", t.getId(), e);
            }
        }
    }
}