package com.solarflow.customer.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

  @Query("""
      select customer from Customer customer
      where customer.companyId = :companyId
        and (:status is null or customer.status = :status)
        and (:q is null or lower(customer.displayName) like lower(concat('%', :q, '%'))
             or lower(coalesce(customer.phone, '')) like lower(concat('%', :q, '%'))
             or lower(coalesce(customer.city, '')) like lower(concat('%', :q, '%')))
      """)
  Page<Customer> search(
      @Param("companyId") UUID companyId,
      @Param("q") String q,
      @Param("status") Customer.Status status,
      Pageable pageable
  );

  Optional<Customer> findByIdAndCompanyId(UUID id, UUID companyId);

  long countByCompanyId(UUID companyId);
}
