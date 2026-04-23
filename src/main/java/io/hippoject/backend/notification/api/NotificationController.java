package io.hippoject.backend.notification.api;

import io.hippoject.backend.notification.dto.NotificationResponse;
import io.hippoject.backend.notification.service.NotificationService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> listNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.listNotifications(jwt);
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long notificationId, @AuthenticationPrincipal Jwt jwt) {
        return notificationService.markRead(notificationId, jwt);
    }
}
