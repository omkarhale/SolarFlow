package com.solarflow.common.tenant;

import com.solarflow.common.exception.NotFoundException;
import java.util.UUID;

public final class TenantGuards {

  private TenantGuards() {
  }

  public static void sameCompany(UUID actorCompanyId, UUID recordCompanyId) {
    if (actorCompanyId == null || recordCompanyId == null || !actorCompanyId.equals(recordCompanyId)) {
      throw new NotFoundException();
    }
  }
}
