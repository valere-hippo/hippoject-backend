package io.hippoject.backend.filter.dto;

import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import java.time.Instant;

public record SavedFilterResponse(
        Long id,
        String name,
        String query,
        Long projectId,
        IssueStatus status,
        IssueType issueType,
        String label,
        Instant createdAt) {
}
