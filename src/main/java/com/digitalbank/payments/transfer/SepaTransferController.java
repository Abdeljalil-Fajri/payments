package com.digitalbank.payments.transfer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers/sepa")
public class SepaTransferController {
    private final SepaTransferService service;

    public SepaTransferController(SepaTransferService service) { this.service = service; }

    // TEMPORARY: customer id from a header, replaced by the JWT later.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferDtos.Response send(@RequestHeader("X-Customer-Id") UUID customerId,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey,
                                      @Valid @RequestBody SepaRequest req) {
        return service.send(customerId, idempotencyKey, req);
    }
}