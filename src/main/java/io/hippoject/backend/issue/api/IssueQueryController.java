package io.hippoject.backend.issue.api;

import io.hippoject.backend.issue.dto.IssueResponse;
import io.hippoject.backend.issue.service.IssueService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<IssueResponse> listIssues() {
        return issueService.listAllIssues();
    }
}
