package io.hippoject.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Ein Projektschlüssel ist erforderlich")
        @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$", message = "Der Projektschlüssel muss aus 2 bis 10 Großbuchstaben oder Ziffern bestehen und mit einem Buchstaben beginnen")
        String key,
        @NotBlank(message = "Ein Projektname ist erforderlich")
        @Size(max = 120, message = "Der Projektname darf höchstens 120 Zeichen lang sein")
        String name,
        @NotBlank(message = "Eine Projektbeschreibung ist erforderlich")
        @Size(max = 500, message = "Die Projektbeschreibung darf höchstens 500 Zeichen lang sein")
        String description) {
}
