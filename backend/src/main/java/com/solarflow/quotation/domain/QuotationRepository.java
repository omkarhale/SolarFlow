package com.solarflow.quotation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

  List<Quotation> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<Quotation> findByIdAndCompanyId(UUID id, UUID companyId);

  List<Quotation> findByCompanyIdAndStatus(UUID companyId, Quotation.Status status);

  List<Quotation> findByCompanyIdAndCustomerIdOrderByCreatedAtDesc(UUID companyId, UUID customerId);
}
