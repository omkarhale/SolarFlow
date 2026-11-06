package com.solarflow.notification;

import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.notification.domain.AppNotification;
import com.solarflow.notification.domain.AppNotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final AppNotificationRepository notifications;
  private final CurrentUser currentUser;

  public NotificationController(AppNotificationRepository notifications, CurrentUser currentUser) {
    this.notifications = notifications;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<NotificationView> list() {
    var actor = currentUser.require();
    return notifications.findTop20ByCompanyIdAndUserIdOrderByCreatedAtDesc(actor.companyId(), actor.id()).stream()
        .map(NotificationView::from)
        .toList();
  }

  @PostMapping("/{id}/read")
  @Transactional
  public NotificationView read(@PathVariable UUID id) {
    var actor = currentUser.require();
    AppNotification notification = notifications.findById(id)
        .filter(item -> actor.companyId().equals(item.getCompanyId()) && actor.id().equals(item.getUserId()))
        .orElseThrow(NotFoundException::new);
    if (notification.getReadAt() == null) {
      notification.setReadAt(Instant.now());
    }
    return NotificationView.from(notification);
  }

  public record NotificationView(UUID id, String title, String body, String category, Instant readAt, Instant createdAt) {
    static NotificationView from(AppNotification notification) {
      return new NotificationView(
          notification.getId(), notification.getTitle(), notification.getBody(), notification.getCategory(),
          notification.getReadAt(), notification.getCreatedAt()
      );
    }
  }
}
