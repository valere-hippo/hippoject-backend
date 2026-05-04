package io.hippoject.backend.chat.dto;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        String authorId,
        String authorDisplayName,
        String body,
        Instant createdAt) {
}
