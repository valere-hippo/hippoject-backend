package io.hippoject.backend.issue.dto;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateIssueRequest(
        @NotBlank(message = "Issue title is required")
        @Size(max = 140, message = "Issue title must be at most 140 characters")
        String title,
        @NotBlank(message = "Issue description is required")
        @Size(max = 2000, message = "Issue description must be at most 2000 characters")
        String description,
        @NotNull(message = "Issue type is required")
        IssueType issueType,
        @NotNull(message = "Issue priority is required")
        IssuePriority priority,
        IssueStatus status,
        Long sprintId,
        Long epicId,
        Set<String> labels,
        @Size(max = 120, message = "Assignee id must be at most 120 characters")
        String assigneeId) {
}
