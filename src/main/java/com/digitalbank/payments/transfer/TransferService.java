package com.digitalbank.payments.transfer;

import com.digitalbank.payments.beneficiary.IbanValidator;
import com.digitalbank.payments.transfer.TransferDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class TransferService {
    private final AccountRepository accounts;
    private final TransferRepository transfers;

    public TransferService(AccountRepository accounts, TransferRepository transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }

    // @Transactional: everything below is ONE database transaction.
    // If any exception is thrown, the debit and the credit are both rolled back.
    @Transactional
    public Response transfer(UUID customerId, String key, Request req) {
        if (key == null || key.isBlank() || key.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Idempotency-Key");
        }

        // Replay of a request we already processed: return the same result, move nothing.
        Optional<Transfer> existing = transfers.findByCustomerIdAndIdempotencyKey(customerId, key);
        if (existing.isPresent()) {
            return Response.from(existing.get());
        }

        if (!IbanValidator.isValid(req.sourceIban()) || !IbanValidator.isValid(req.destinationIban())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }
        String src = IbanValidator.normalize(req.sourceIban());
        String dst = IbanValidator.normalize(req.destinationIban());
        if (src.equals(dst)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination must differ");
        }

        List<Account> locked = accounts.lockByIbans(Set.of(src, dst));
        Account from = find(locked, src);
        Account to = find(locked, dst);

        // Same message whether the account is missing or belongs to someone else,
        // so an attacker cannot discover which IBANs exist.
        if (from == null || to == null || !from.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency mismatch");
        }
        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "Insufficient funds");
        }

        from.debit(req.amount());
        to.credit(req.amount());

        Transfer saved = transfers.save(new Transfer(customerId, key, src, dst,
                req.amount(), from.getCurrency(), "COMPLETED", req.reference()));
        return Response.from(saved);
    }

    private Account find(List<Account> list, String iban) {
        return list.stream().filter(a -> a.getIban().equals(iban)).findFirst().orElse(null);
    }
}