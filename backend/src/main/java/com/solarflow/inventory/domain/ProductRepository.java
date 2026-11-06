package com.solarflow.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  List<Product> findByCompanyIdOrderByNameAsc(UUID companyId);

  Optional<Product> findByIdAndCompanyId(UUID id, UUID companyId);
}
