package io.hippoject.backend.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String type,
        Long projectId,
        Long issueId,
        String message,
        boolean read,
        Instant createdAt) {
}
