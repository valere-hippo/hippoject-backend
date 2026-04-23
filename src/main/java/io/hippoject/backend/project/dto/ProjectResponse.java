package io.hippoject.backend.project.dto;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String key,
        String name,
        String description,
        String ownerId,
        Instant createdAt,
        long issueCount,
        long memberCount) {
}
