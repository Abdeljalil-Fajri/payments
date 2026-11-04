package com.digitalbank.payments.transfer;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    // Locks the rows until the transaction ends. Always locking in the same
    // order (by id) prevents two opposite transfers from deadlocking.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.iban in :ibans order by a.id")
    List<Account> lockByIbans(@Param("ibans") Collection<String> ibans);
}