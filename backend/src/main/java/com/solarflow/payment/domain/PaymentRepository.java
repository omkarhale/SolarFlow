package com.solarflow.payment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  List<Payment> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<Payment> findByIdAndCompanyId(UUID id, UUID companyId);

  List<Payment> findByCompanyIdAndCustomerIdOrderByCreatedAtDesc(UUID companyId, UUID customerId);

  List<Payment> findByCompanyIdAndStatus(UUID companyId, Payment.Status status);
}
