package io.hippoject.backend.identity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateIdentityUserRolesRequest(
        @NotNull(message = "Mindestens eine Rolle ist erforderlich")
        List<String> realmRoles) {
}
