package io.hippoject.backend.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSprintRequest(
        @NotBlank(message = "Sprint name is required")
        @Size(max = 120, message = "Sprint name must be at most 120 characters")
        String name,
        @NotBlank(message = "Sprint goal is required")
        @Size(max = 1000, message = "Sprint goal must be at most 1000 characters")
        String goal,
        @NotNull(message = "Sprint start date is required")
        LocalDate startsAt,
        @NotNull(message = "Sprint end date is required")
        LocalDate endsAt,
        boolean active) {
}
