package io.hippoject.backend.projectmember.dto;

import io.hippoject.backend.projectmember.domain.ProjectRole;
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
        @NotNull(message = "Project role is required")
        ProjectRole role) {
}
