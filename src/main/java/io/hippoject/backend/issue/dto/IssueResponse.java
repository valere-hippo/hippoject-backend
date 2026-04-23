package io.hippoject.backend.issue.dto;

import io.hippoject.backend.comment.dto.CommentResponse;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record IssueResponse(
        Long id,
        String issueKey,
        Long projectId,
        String projectKey,
        String title,
        String description,
        IssueStatus status,
        IssueType issueType,
        IssuePriority priority,
        Long sprintId,
        String sprintName,
        Long epicId,
        String epicKey,
        String epicTitle,
        Set<String> labels,
        String assigneeId,
        String reporterId,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> comments) {
}
