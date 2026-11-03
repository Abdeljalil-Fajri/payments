package com.digitalbank.payments.beneficiary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    List<Beneficiary> findByCustomerId(UUID customerId);
    boolean existsByCustomerIdAndIban(UUID customerId, String iban);
}