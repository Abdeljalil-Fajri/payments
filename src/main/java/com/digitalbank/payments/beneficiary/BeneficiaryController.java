package com.digitalbank.payments.beneficiary;

import com.digitalbank.payments.beneficiary.BeneficiaryDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) { this.service = service; }

    // TEMPORARY: the customer id comes from a header. We replace this with
    // the authenticated user (JWT) when we add Spring Security.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response add(@RequestHeader("X-Customer-Id") UUID customerId,
                        @Valid @RequestBody CreateRequest req) {
        return service.add(customerId, req);
    }

    @GetMapping
    public List<Response> list(@RequestHeader("X-Customer-Id") UUID customerId) {
        return service.list(customerId);
    }
}