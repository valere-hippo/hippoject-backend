package io.hippoject.backend.issue.api;

import io.hippoject.backend.issue.dto.CreateIssueRequest;
import io.hippoject.backend.issue.dto.IssueResponse;
import io.hippoject.backend.issue.dto.UpdateIssueRequest;
import io.hippoject.backend.issue.service.IssueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager', 'developer', 'reporter')")
    public IssueResponse createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return issueService.createIssue(projectId, request, jwt);
    }

    @GetMapping
    public List<IssueResponse> listIssues(@PathVariable Long projectId) {
        return issueService.listIssues(projectId);
    }

    @GetMapping("/{issueId}")
    public IssueResponse getIssue(@PathVariable Long projectId, @PathVariable Long issueId) {
        return issueService.getIssue(projectId, issueId);
    }

    @PutMapping("/{issueId}")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager', 'developer')")
    public IssueResponse updateIssue(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody UpdateIssueRequest request) {
        return issueService.updateIssue(projectId, issueId, request);
    }
}
