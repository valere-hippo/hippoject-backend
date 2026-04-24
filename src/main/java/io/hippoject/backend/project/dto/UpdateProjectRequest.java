package io.hippoject.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "Ein Projektname ist erforderlich")
        @Size(max = 120, message = "Der Projektname darf höchstens 120 Zeichen lang sein")
        String name,
        @NotBlank(message = "Eine Projektbeschreibung ist erforderlich")
        @Size(max = 500, message = "Die Projektbeschreibung darf höchstens 500 Zeichen lang sein")
        String description) {
}
