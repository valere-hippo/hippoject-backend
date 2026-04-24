package io.hippoject.backend.issue.dto;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateIssueRequest(
        @NotBlank(message = "Ein Vorgangstitel ist erforderlich")
        @Size(max = 140, message = "Der Vorgangstitel darf höchstens 140 Zeichen lang sein")
        String title,
        @NotBlank(message = "Eine Vorgangsbeschreibung ist erforderlich")
        @Size(max = 2000, message = "Die Vorgangsbeschreibung darf höchstens 2000 Zeichen lang sein")
        String description,
        @NotNull(message = "Ein Vorgangstyp ist erforderlich")
        IssueType issueType,
        @NotNull(message = "Ein Status ist erforderlich")
        IssueStatus status,
        @NotNull(message = "Eine Priorität ist erforderlich")
        IssuePriority priority,
        Long sprintId,
        Long epicId,
        Set<String> labels,
        @Size(max = 120, message = "Die Kennung der zuständigen Person darf höchstens 120 Zeichen lang sein")
        String assigneeId) {
}
