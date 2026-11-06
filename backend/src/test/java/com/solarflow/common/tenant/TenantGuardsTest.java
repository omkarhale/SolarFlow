package com.solarflow.common.tenant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.solarflow.common.exception.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantGuardsTest {

  @Test
  void allowsMatchingCompany() {
    UUID companyId = UUID.randomUUID();
    assertThatCode(() -> TenantGuards.sameCompany(companyId, companyId)).doesNotThrowAnyException();
  }

  @Test
  void hidesCrossTenantRecords() {
    assertThatThrownBy(() -> TenantGuards.sameCompany(UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void hidesMissingTenant() {
    assertThatThrownBy(() -> TenantGuards.sameCompany(null, UUID.randomUUID()))
        .isInstanceOf(NotFoundException.class);
  }
}
