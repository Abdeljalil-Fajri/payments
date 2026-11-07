package com.digitalbank.payments.transfer;

import com.digitalbank.payments.beneficiary.Beneficiary;
import com.digitalbank.payments.beneficiary.BeneficiaryRepository;
import com.digitalbank.payments.beneficiary.IbanValidator;
import com.digitalbank.payments.transfer.TransferDtos.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class InstantTransferService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("5000.00");

    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final BeneficiaryRepository beneficiaries;
    private final SimulatedBank bank;

    public InstantTransferService(AccountRepository accounts, TransferRepository transfers,
                                  BeneficiaryRepository beneficiaries, SimulatedBank bank) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.beneficiaries = beneficiaries;
        this.bank = bank;
    }

    @Transactional
    public Response send(UUID customerId, String key, SepaRequest req) {
        if (key == null || key.isBlank() || key.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Idempotency-Key");
        }

        Optional<Transfer> existing = transfers.findByCustomerIdAndIdempotencyKey(customerId, key);
        if (existing.isPresent()) {
            return Response.from(existing.get());
        }

        if (!IbanValidator.isValid(req.sourceIban())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }
        String src = IbanValidator.normalize(req.sourceIban());

        Beneficiary beneficiary = beneficiaries.findById(req.beneficiaryId())
                .filter(b -> b.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
        String dst = beneficiary.getIban();

        if (src.equals(dst)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination must differ");
        }
        if (req.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "Amount exceeds the instant payment limit");
        }

        List<Account> locked = accounts.lockByIbans(Set.of(src));
        Account from = locked.isEmpty() ? null : locked.get(0);

        if (from == null || !from.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        if (!"EUR".equals(from.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instant payments require a EUR account");
        }
        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "Insufficient funds");
        }

        // Ask the receiving bank BEFORE touching the balance.
        if (bank.isRejected(dst)) {
            Transfer failed = new Transfer(customerId, key, src, dst, req.amount(), "EUR",
                    TransferStatus.PENDING, req.reference(), TransferType.INSTANT);
            failed.markFailed("REJECTED_BY_BANK");
            return Response.from(transfers.save(failed));
        }

        from.debit(req.amount());
        Transfer done = transfers.save(new Transfer(customerId, key, src, dst, req.amount(), "EUR",
                TransferStatus.COMPLETED, req.reference(), TransferType.INSTANT));
        return Response.from(done);
    }
}