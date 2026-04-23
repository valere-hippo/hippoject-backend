package io.hippoject.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project key is required")
        @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$", message = "Project key must be 2-10 uppercase letters or digits and start with a letter")
        String key,
        @NotBlank(message = "Project name is required")
        @Size(max = 120, message = "Project name must be at most 120 characters")
        String name,
        @NotBlank(message = "Project description is required")
        @Size(max = 500, message = "Project description must be at most 500 characters")
        String description) {
}
