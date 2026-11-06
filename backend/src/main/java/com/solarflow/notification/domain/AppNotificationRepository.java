package com.solarflow.notification.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {

  List<AppNotification> findTop20ByCompanyIdAndUserIdOrderByCreatedAtDesc(UUID companyId, UUID userId);
}
