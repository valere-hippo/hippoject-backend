package io.hippoject.backend.issue.dto;

import io.hippoject.backend.comment.dto.CommentResponse;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import java.time.Instant;
import java.util.List;

public record IssueResponse(
        Long id,
        String issueKey,
        Long projectId,
        String projectKey,
        String title,
        String description,
        IssueStatus status,
        IssuePriority priority,
        Long sprintId,
        String sprintName,
        String assigneeId,
        String reporterId,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> comments) {
}
