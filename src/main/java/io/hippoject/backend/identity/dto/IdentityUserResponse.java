package io.hippoject.backend.identity.dto;

public record IdentityUserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String displayName,
        boolean emailVerified,
        boolean enabled) {
}
