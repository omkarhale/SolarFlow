package com.solarflow.quotation.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationLineRepository extends JpaRepository<QuotationLine, UUID> {

  List<QuotationLine> findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(UUID quotationId, UUID companyId);

  void deleteByQuotationIdAndCompanyId(UUID quotationId, UUID companyId);
}
