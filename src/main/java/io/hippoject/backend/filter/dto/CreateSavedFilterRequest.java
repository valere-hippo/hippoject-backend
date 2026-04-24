package io.hippoject.backend.filter.dto;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSavedFilterRequest(
        @NotBlank(message = "Ein Filtername ist erforderlich")
        @Size(max = 120, message = "Der Filtername darf höchstens 120 Zeichen lang sein")
        String name,
        @Size(max = 200, message = "Die Suchanfrage darf höchstens 200 Zeichen lang sein")
        String query,
        Long projectId,
        IssueStatus status,
        IssueType issueType,
        IssuePriority priority,
        @Size(max = 120, message = "Die Kennung der zuständigen Person darf höchstens 120 Zeichen lang sein")
        String assigneeId,
        @Size(max = 50, message = "Ein Label darf höchstens 50 Zeichen lang sein")
        String label) {
}
