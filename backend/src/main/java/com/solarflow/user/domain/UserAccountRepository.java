package com.solarflow.user.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  Optional<UserAccount> findByEmailIgnoreCase(String email);

  List<UserAccount> findByCompanyIdOrderByFirstNameAsc(UUID companyId);

  boolean existsByEmailIgnoreCase(String email);
}
