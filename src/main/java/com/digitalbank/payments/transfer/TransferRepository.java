package com.digitalbank.payments.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Optional<Transfer> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

    Optional<Transfer> findByIdAndCustomerId(UUID id, UUID customerId);

    List<Transfer> findTop50ByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Transfer> findByTypeAndStatus(TransferType type, TransferStatus status);
}