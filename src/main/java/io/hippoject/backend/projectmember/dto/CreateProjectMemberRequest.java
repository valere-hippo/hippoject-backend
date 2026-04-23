package io.hippoject.backend.projectmember.dto;

import io.hippoject.backend.projectmember.domain.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectMemberRequest(
        @NotBlank(message = "User id is required")
        @Size(max = 120, message = "User id must be at most 120 characters")
        String userId,
        @NotBlank(message = "Display name is required")
        @Size(max = 120, message = "Display name must be at most 120 characters")
        String displayName,
        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must be at most 160 characters")
        String email,
        @NotNull(message = "Project role is required")
        ProjectRole role) {
}
