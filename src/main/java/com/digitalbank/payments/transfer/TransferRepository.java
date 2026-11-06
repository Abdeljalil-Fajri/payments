package com.digitalbank.payments.transfer;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Optional<Transfer> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

    Optional<Transfer> findByIdAndCustomerId(UUID id, UUID customerId);

    List<Transfer> findTop50ByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Transfer> findByTypeAndStatus(TransferType type, TransferStatus status);

    // Locks the transfer row so two processes can never settle the same transfer.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transfer t where t.id = :id")
    Optional<Transfer> lockById(@Param("id") UUID id);
}