package com.solarflow.warranty.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {

  List<Warranty> findByCompanyIdOrderByEndDateAsc(UUID companyId);

  Optional<Warranty> findByIdAndCompanyId(UUID id, UUID companyId);

  List<Warranty> findByCompanyIdAndEndDateLessThanEqualOrderByEndDateAsc(UUID companyId, LocalDate endDate);
}
