package io.hippoject.backend.chat.dto;

import java.time.Instant;

public record ProjectChatMessageResponse(
        Long id,
        Long projectId,
        String authorId,
        String authorDisplayName,
        String body,
        Instant createdAt) {
}
