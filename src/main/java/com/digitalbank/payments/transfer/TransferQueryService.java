package com.digitalbank.payments.transfer;

import com.digitalbank.payments.transfer.TransferDtos.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class TransferQueryService {
    private final TransferRepository transfers;

    public TransferQueryService(TransferRepository transfers) {
        this.transfers = transfers;
    }

    public Response get(UUID customerId, UUID transferId) {
        // Same 404 whether the transfer doesn't exist or belongs to someone else.
        return transfers.findByIdAndCustomerId(transferId, customerId)
                .map(Response::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
    }

    public List<Response> list(UUID customerId) {
        return transfers.findTop50ByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(Response::from).toList();
    }
}