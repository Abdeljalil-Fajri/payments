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
public class SepaTransferService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final BeneficiaryRepository beneficiaries;

    public SepaTransferService(AccountRepository accounts, TransferRepository transfers,
                               BeneficiaryRepository beneficiaries) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.beneficiaries = beneficiaries;
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

        // Same 404 whether the beneficiary doesn't exist or belongs to someone else.
        Beneficiary beneficiary = beneficiaries.findById(req.beneficiaryId())
                .filter(b -> b.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
        String dst = beneficiary.getIban();

        if (src.equals(dst)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination must differ");
        }
        if (req.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "Amount exceeds the SEPA limit");
        }

        List<Account> locked = accounts.lockByIbans(Set.of(src));
        Account from = locked.isEmpty() ? null : locked.get(0);

        if (from == null || !from.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        if (!"EUR".equals(from.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SEPA transfers require a EUR account");
        }
        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "Insufficient funds");
        }

        from.debit(req.amount());

        Transfer saved = transfers.save(new Transfer(customerId, key, src, dst,
                req.amount(), "EUR", TransferStatus.PENDING, req.reference(), TransferType.SEPA));
        return Response.from(saved);
    }
}