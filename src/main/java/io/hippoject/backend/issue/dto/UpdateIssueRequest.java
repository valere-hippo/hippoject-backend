package io.hippoject.backend.issue.dto;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateIssueRequest(
        @NotBlank(message = "Issue title is required")
        @Size(max = 140, message = "Issue title must be at most 140 characters")
        String title,
        @NotBlank(message = "Issue description is required")
        @Size(max = 2000, message = "Issue description must be at most 2000 characters")
        String description,
        @NotNull(message = "Issue status is required")
        IssueStatus status,
        @NotNull(message = "Issue priority is required")
        IssuePriority priority,
        Long sprintId,
        @Size(max = 120, message = "Assignee id must be at most 120 characters")
        String assigneeId) {
}
