package io.hippoject.backend.issue.service;

import io.hippoject.backend.audit.service.AuditEventService;
import io.hippoject.backend.comment.dto.CommentResponse;
import io.hippoject.backend.comment.service.CommentMapper;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.issue.domain.Issue;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import io.hippoject.backend.issue.dto.CreateIssueRequest;
import io.hippoject.backend.issue.dto.IssueResponse;
import io.hippoject.backend.issue.dto.UpdateIssueRequest;
import io.hippoject.backend.issue.repository.IssueRepository;
import io.hippoject.backend.notification.service.NotificationService;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.sprint.domain.Sprint;
import io.hippoject.backend.sprint.service.SprintService;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final CommentMapper commentMapper;
    private final SprintService sprintService;
    private final AuditEventService auditEventService;
    private final NotificationService notificationService;

    public IssueService(IssueRepository issueRepository, ProjectService projectService, CommentMapper commentMapper, SprintService sprintService, AuditEventService auditEventService, NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.projectService = projectService;
        this.commentMapper = commentMapper;
        this.sprintService = sprintService;
        this.auditEventService = auditEventService;
        this.notificationService = notificationService;
    }

    @Transactional
    public IssueResponse createIssue(Long projectId, CreateIssueRequest request, Jwt jwt) {
        Project project = projectService.findProject(projectId);
        Instant now = Instant.now();
        Sprint sprint = sprintService.resolveSprint(projectId, request.sprintId());
        Issue epic = request.issueType() == IssueType.EPIC ? null : resolveEpic(projectId, request.epicId());
        Issue issue = new Issue(
                project,
                nextIssueKey(project),
                request.title().trim(),
                request.description().trim(),
                request.status() != null ? request.status() : IssueStatus.TODO,
                request.issueType(),
                request.priority(),
                trimToNull(request.assigneeId()),
                sprint,
                epic,
                actorId(jwt),
                now,
                now,
                normalizeLabels(request.labels()));

        Issue savedIssue = issueRepository.save(issue);
        auditEventService.record(projectId, "ISSUE_CREATED", "Issue created", savedIssue.getIssueKey() + " · " + savedIssue.getTitle());
        notificationService.notifyAssignee(savedIssue, savedIssue.getIssueKey() + " was assigned to you", actorId(jwt));
        return toResponse(savedIssue);
    }

    public List<IssueResponse> listIssues(Long projectId) {
        projectService.findProject(projectId);
        return issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .filter((issue) -> issue.getDeletedAt() == null)
                .map(this::toResponse)
                .toList();
    }

    public List<IssueResponse> listAllIssues(String query, Long projectId, IssueStatus status, IssueType issueType, IssuePriority priority, String assigneeId, String label) {
        return issueRepository.findAllByOrderByUpdatedAtDesc().stream()
                .filter((issue) -> issue.getDeletedAt() == null)
                .filter((issue) -> projectId == null || issue.getProject().getId().equals(projectId))
                .filter((issue) -> status == null || issue.getStatus() == status)
                .filter((issue) -> issueType == null || issue.getIssueType() == issueType)
                .filter((issue) -> priority == null || issue.getPriority() == priority)
                .filter((issue) -> assigneeId == null || assigneeId.isBlank() || (issue.getAssigneeId() != null && issue.getAssigneeId().equalsIgnoreCase(assigneeId.trim())))
                .filter((issue) -> label == null || issue.getLabels().stream().anyMatch((item) -> item.equalsIgnoreCase(label.trim())))
                .filter((issue) -> matchesQuery(issue, query))
                .map(this::toResponse)
                .toList();
    }

    public IssueResponse getIssue(Long projectId, Long issueId) {
        return toResponse(findIssue(projectId, issueId));
    }

    @Transactional
    public IssueResponse updateIssue(Long projectId, Long issueId, UpdateIssueRequest request) {
        Issue issue = findIssue(projectId, issueId);
        String previousAssigneeId = issue.getAssigneeId();
        issue.setTitle(request.title().trim());
        issue.setDescription(request.description().trim());
        issue.setIssueType(request.issueType());
        issue.setStatus(request.status());
        issue.setPriority(request.priority());
        issue.setAssigneeId(trimToNull(request.assigneeId()));
        issue.setSprint(sprintService.resolveSprint(projectId, request.sprintId()));
        issue.setEpic(request.issueType() == IssueType.EPIC ? null : resolveEpic(projectId, request.epicId()));
        issue.setLabels(normalizeLabels(request.labels()));
        issue.setUpdatedAt(Instant.now());
        auditEventService.record(projectId, "ISSUE_UPDATED", "Issue updated", issue.getIssueKey() + " moved to " + issue.getStatus());
        if (issue.getAssigneeId() != null && !issue.getAssigneeId().equalsIgnoreCase(previousAssigneeId != null ? previousAssigneeId : "")) {
            notificationService.notifyAssignee(issue, issue.getIssueKey() + " was assigned to you", issue.getReporterId());
        }
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse deleteIssue(Long projectId, Long issueId) {
        Issue issue = findIssue(projectId, issueId);
        issueRepository.findByEpicId(issueId).forEach((child) -> child.setEpic(null));
        issue.setDeletedAt(Instant.now());
        issue.setSprint(null);
        auditEventService.record(projectId, "ISSUE_DELETED", "Issue archived", issue.getIssueKey() + " was archived");
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse restoreIssue(Long projectId, Long issueId) {
        Issue issue = findIssueIncludingDeleted(projectId, issueId);
        issue.setDeletedAt(null);
        issue.setUpdatedAt(Instant.now());
        auditEventService.record(projectId, "ISSUE_RESTORED", "Issue restored", issue.getIssueKey() + " was restored");
        return toResponse(issue);
    }

    public Issue findIssue(Long projectId, Long issueId) {
        Issue issue = findIssueIncludingDeleted(projectId, issueId);
        if (issue.getDeletedAt() != null) {
            throw new NotFoundException("Issue not found: " + issueId + " in project " + projectId);
        }
        return issue;
    }

    public Issue findIssueIncludingDeleted(Long projectId, Long issueId) {
        projectService.findProject(projectId);
        return issueRepository.findByProjectIdAndId(projectId, issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found: " + issueId + " in project " + projectId));
    }

    public IssueResponse toResponse(Issue issue) {
        List<CommentResponse> comments = issue.getComments().stream()
                .map(commentMapper::toResponse)
                .toList();
        long epicProgressTotal = issue.getIssueType() == IssueType.EPIC ? issueRepository.countByEpicId(issue.getId()) : 0;
        long epicProgressDone = issue.getIssueType() == IssueType.EPIC ? issueRepository.countByEpicIdAndStatus(issue.getId(), IssueStatus.DONE) : 0;

        return new IssueResponse(
                issue.getId(),
                issue.getIssueKey(),
                issue.getProject().getId(),
                issue.getProject().getKey(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getIssueType(),
                issue.getPriority(),
                issue.getSprint() != null ? issue.getSprint().getId() : null,
                issue.getSprint() != null ? issue.getSprint().getName() : null,
                issue.getEpic() != null ? issue.getEpic().getId() : null,
                issue.getEpic() != null ? issue.getEpic().getIssueKey() : null,
                issue.getEpic() != null ? issue.getEpic().getTitle() : null,
                issue.getLabels(),
                epicProgressTotal,
                epicProgressDone,
                issue.getAssigneeId(),
                issue.getReporterId(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getDeletedAt(),
                comments);
    }

    private String nextIssueKey(Project project) {
        long issueNumber = issueRepository.countByProjectId(project.getId()) + 1;
        return project.getKey() + "-" + issueNumber;
    }

    private String actorId(Jwt jwt) {
        if (jwt == null) {
            return "local-dev";
        }
        if (jwt.getClaimAsString("preferred_username") != null) {
            return jwt.getClaimAsString("preferred_username");
        }
        return jwt.getSubject() != null ? jwt.getSubject() : "local-dev";
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean matchesQuery(Issue issue, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return issue.getIssueKey().toLowerCase(Locale.ROOT).contains(needle)
                || issue.getTitle().toLowerCase(Locale.ROOT).contains(needle)
                || issue.getDescription().toLowerCase(Locale.ROOT).contains(needle)
                || issue.getProject().getKey().toLowerCase(Locale.ROOT).contains(needle);
    }

    private Set<String> normalizeLabels(Set<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return labels.stream()
                .filter((label) -> label != null && !label.isBlank())
                .map(String::trim)
                .limit(10)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Issue resolveEpic(Long projectId, Long epicId) {
        if (epicId == null) {
            return null;
        }
        Issue epic = findIssue(projectId, epicId);
        if (epic.getIssueType() != IssueType.EPIC) {
            throw new NotFoundException("Epic not found: " + epicId + " in project " + projectId);
        }
        return epic;
    }
}
