package io.hippoject.backend.comment.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String body,
        String authorId,
        Instant createdAt) {
}
