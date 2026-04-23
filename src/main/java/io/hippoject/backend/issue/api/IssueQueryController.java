package io.hippoject.backend.issue.api;

import io.hippoject.backend.issue.dto.IssueResponse;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import io.hippoject.backend.issue.service.IssueService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueQueryController {

    private final IssueService issueService;

    public IssueQueryController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public List<IssueResponse> listIssues(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssueType issueType,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String label,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        return issueService.listAllIssues(query, projectId, status, issueType, priority, assigneeId, label, includeArchived);
    }
}
