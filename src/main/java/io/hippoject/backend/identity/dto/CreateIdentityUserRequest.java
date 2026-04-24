package io.hippoject.backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIdentityUserRequest(
        @NotBlank(message = "Ein Benutzername ist erforderlich")
        @Size(max = 120, message = "Der Benutzername darf höchstens 120 Zeichen lang sein")
        String username,
        @NotBlank(message = "Eine E-Mail-Adresse ist erforderlich")
        @Email(message = "Die E-Mail-Adresse ist ungültig")
        @Size(max = 160, message = "Die E-Mail-Adresse darf höchstens 160 Zeichen lang sein")
        String email,
        @NotBlank(message = "Ein Vorname ist erforderlich")
        @Size(max = 120, message = "Der Vorname darf höchstens 120 Zeichen lang sein")
        String firstName,
        @NotBlank(message = "Ein Nachname ist erforderlich")
        @Size(max = 120, message = "Der Nachname darf höchstens 120 Zeichen lang sein")
        String lastName) {
}
