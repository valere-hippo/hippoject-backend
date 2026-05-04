package io.hippoject.backend.identity.dto;

import jakarta.validation.constraints.Size;

public record UpdateIdentityProfileRequest(
        @Size(max = 300000, message = "Das Profilbild ist zu groß")
        String avatarUrl) {
}
