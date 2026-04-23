package io.hippoject.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 120, message = "Project name must be at most 120 characters")
        String name,
        @NotBlank(message = "Project description is required")
        @Size(max = 500, message = "Project description must be at most 500 characters")
        String description) {
}
