package com.digitalbank.payments.transfer;

import com.digitalbank.payments.transfer.TransferDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
    private final TransferService service;

    public TransferController(TransferService service) { this.service = service; }

    // TEMPORARY: customer id from a header, replaced by the JWT later.
    @PostMapping("/internal")
    @ResponseStatus(HttpStatus.CREATED)
    public Response internal(@RequestHeader("X-Customer-Id") UUID customerId,
                             @RequestHeader("Idempotency-Key") String idempotencyKey,
                             @Valid @RequestBody Request req) {
        return service.transfer(customerId, idempotencyKey, req);
    }
}