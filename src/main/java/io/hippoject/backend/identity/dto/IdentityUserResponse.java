package io.hippoject.backend.identity.dto;

import java.util.List;

public record IdentityUserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String displayName,
        boolean emailVerified,
        boolean enabled,
        List<String> realmRoles) {
}
