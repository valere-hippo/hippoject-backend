package io.hippoject.backend.issue.service;

import io.hippoject.backend.comment.dto.CommentResponse;
import io.hippoject.backend.comment.service.CommentMapper;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.issue.domain.Issue;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.dto.CreateIssueRequest;
import io.hippoject.backend.issue.dto.IssueResponse;
import io.hippoject.backend.issue.dto.UpdateIssueRequest;
import io.hippoject.backend.issue.repository.IssueRepository;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final CommentMapper commentMapper;

    public IssueService(IssueRepository issueRepository, ProjectService projectService, CommentMapper commentMapper) {
        this.issueRepository = issueRepository;
        this.projectService = projectService;
        this.commentMapper = commentMapper;
    }

    @Transactional
    public IssueResponse createIssue(Long projectId, CreateIssueRequest request, Jwt jwt) {
        Project project = projectService.findProject(projectId);
        Instant now = Instant.now();
        Issue issue = new Issue(
                project,
                nextIssueKey(project),
                request.title().trim(),
                request.description().trim(),
                request.status() != null ? request.status() : IssueStatus.TODO,
                request.priority(),
                trimToNull(request.assigneeId()),
                actorId(jwt),
                now,
                now);

        return toResponse(issueRepository.save(issue));
    }

    public List<IssueResponse> listIssues(Long projectId) {
        projectService.findProject(projectId);
        return issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    public IssueResponse getIssue(Long projectId, Long issueId) {
        return toResponse(findIssue(projectId, issueId));
    }

    @Transactional
    public IssueResponse updateIssue(Long projectId, Long issueId, UpdateIssueRequest request) {
        Issue issue = findIssue(projectId, issueId);
        issue.setTitle(request.title().trim());
        issue.setDescription(request.description().trim());
        issue.setStatus(request.status());
        issue.setPriority(request.priority());
        issue.setAssigneeId(trimToNull(request.assigneeId()));
        issue.setUpdatedAt(Instant.now());
        return toResponse(issue);
    }

    public Issue findIssue(Long projectId, Long issueId) {
        projectService.findProject(projectId);
        return issueRepository.findByProjectIdAndId(projectId, issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found: " + issueId + " in project " + projectId));
    }

    public IssueResponse toResponse(Issue issue) {
        List<CommentResponse> comments = issue.getComments().stream()
                .map(commentMapper::toResponse)
                .toList();

        return new IssueResponse(
                issue.getId(),
                issue.getIssueKey(),
                issue.getProject().getId(),
                issue.getProject().getKey(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getAssigneeId(),
                issue.getReporterId(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                comments);
    }

    private String nextIssueKey(Project project) {
        long issueNumber = issueRepository.countByProjectId(project.getId()) + 1;
        return project.getKey() + "-" + issueNumber;
    }

    private String actorId(Jwt jwt) {
        return jwt.getSubject() != null ? jwt.getSubject() : jwt.getClaimAsString("preferred_username");
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
