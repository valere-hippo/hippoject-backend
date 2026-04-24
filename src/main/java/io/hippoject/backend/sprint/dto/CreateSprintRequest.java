package io.hippoject.backend.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSprintRequest(
        @NotBlank(message = "Ein Sprint-Name ist erforderlich")
        @Size(max = 120, message = "Der Sprint-Name darf höchstens 120 Zeichen lang sein")
        String name,
        @NotBlank(message = "Ein Sprint-Ziel ist erforderlich")
        @Size(max = 1000, message = "Das Sprint-Ziel darf höchstens 1000 Zeichen lang sein")
        String goal,
        @NotNull(message = "Ein Startdatum ist erforderlich")
        LocalDate startsAt,
        @NotNull(message = "Ein Enddatum ist erforderlich")
        LocalDate endsAt,
        boolean active) {
}
