package com.digitalbank.payments.beneficiary;

import com.digitalbank.payments.beneficiary.BeneficiaryDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class BeneficiaryService {
    private final BeneficiaryRepository repo;

    public BeneficiaryService(BeneficiaryRepository repo) { this.repo = repo; }

    public Response add(UUID customerId, CreateRequest req) {
        if (!IbanValidator.isValid(req.iban())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }
        String iban = IbanValidator.normalize(req.iban());
        if (repo.existsByCustomerIdAndIban(customerId, iban)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Beneficiary already exists");
        }
        return Response.from(repo.save(new Beneficiary(customerId, req.name().trim(), iban, req.bic())));
    }

    public List<Response> list(UUID customerId) {
        return repo.findByCustomerId(customerId).stream().map(Response::from).toList();
    }
}