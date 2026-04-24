package io.hippoject.backend.projectmember.dto;

import io.hippoject.backend.projectmember.domain.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectMemberRequest(
        @NotBlank(message = "Eine Benutzerkennung ist erforderlich")
        @Size(max = 120, message = "Die Benutzerkennung darf höchstens 120 Zeichen lang sein")
        String userId,
        @NotBlank(message = "Ein Anzeigename ist erforderlich")
        @Size(max = 120, message = "Der Anzeigename darf höchstens 120 Zeichen lang sein")
        String displayName,
        @Email(message = "Die E-Mail-Adresse ist ungültig")
        @Size(max = 160, message = "Die E-Mail-Adresse darf höchstens 160 Zeichen lang sein")
        String email,
        @NotNull(message = "Eine Projektrolle ist erforderlich")
        ProjectRole role) {
}
