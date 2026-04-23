package io.hippoject.backend.sprint.dto;

import io.hippoject.backend.sprint.domain.SprintStatus;
import java.time.Instant;
import java.time.LocalDate;

public record SprintResponse(
        Long id,
        Long projectId,
        String name,
        String goal,
        LocalDate startsAt,
        LocalDate endsAt,
        SprintStatus status,
        boolean active,
        Instant completedAt,
        Instant createdAt,
        long issueCount) {
}
