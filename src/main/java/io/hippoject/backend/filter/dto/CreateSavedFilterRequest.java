package io.hippoject.backend.filter.dto;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSavedFilterRequest(
        @NotBlank(message = "Filter name is required")
        @Size(max = 120, message = "Filter name must be at most 120 characters")
        String name,
        @Size(max = 200, message = "Query must be at most 200 characters")
        String query,
        Long projectId,
        IssueStatus status,
        IssueType issueType,
        IssuePriority priority,
        @Size(max = 120, message = "Assignee id must be at most 120 characters")
        String assigneeId,
        @Size(max = 50, message = "Label must be at most 50 characters")
        String label) {
}
