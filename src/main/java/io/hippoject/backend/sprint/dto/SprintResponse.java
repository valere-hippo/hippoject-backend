package io.hippoject.backend.sprint.dto;

import java.time.Instant;
import java.time.LocalDate;

public record SprintResponse(
        Long id,
        Long projectId,
        String name,
        String goal,
        LocalDate startsAt,
        LocalDate endsAt,
        boolean active,
        Instant createdAt,
        long issueCount) {
}
