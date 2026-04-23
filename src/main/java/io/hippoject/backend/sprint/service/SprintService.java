package io.hippoject.backend.sprint.service;

import io.hippoject.backend.audit.service.AuditEventService;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.issue.repository.IssueRepository;
import io.hippoject.backend.notification.service.NotificationService;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.sprint.domain.Sprint;
import io.hippoject.backend.sprint.domain.SprintStatus;
import io.hippoject.backend.sprint.dto.CreateSprintRequest;
import io.hippoject.backend.sprint.dto.SprintResponse;
import io.hippoject.backend.sprint.repository.SprintRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final AuditEventService auditEventService;
    private final IssueRepository issueRepository;
    private final NotificationService notificationService;

    public SprintService(SprintRepository sprintRepository, ProjectService projectService, AuditEventService auditEventService, IssueRepository issueRepository, NotificationService notificationService) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.auditEventService = auditEventService;
        this.issueRepository = issueRepository;
        this.notificationService = notificationService;
    }

    public List<SprintResponse> listSprints(Long projectId) {
        projectService.findProject(projectId);
        return sprintRepository.findByProjectIdOrderByStartsAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SprintResponse createSprint(Long projectId, CreateSprintRequest request) {
        Project project = projectService.findProject(projectId);
        if (request.active()) {
            deactivateActiveSprints(projectId);
        }
        Sprint sprint = new Sprint(
                project,
                request.name().trim(),
                request.goal().trim(),
                request.startsAt(),
                request.endsAt(),
                request.active(),
                Instant.now(),
                null);
        Sprint savedSprint = sprintRepository.save(sprint);
        auditEventService.record(projectId, "SPRINT_CREATED", "Sprint created", savedSprint.getName() + " was planned");
        return toResponse(savedSprint);
    }

    @Transactional
    public SprintResponse startSprint(Long projectId, Long sprintId) {
        Sprint sprint = findSprint(projectId, sprintId);
        deactivateActiveSprints(projectId);
        sprint.setActive(true);
        sprint.setCompletedAt(null);
        auditEventService.record(projectId, "SPRINT_STARTED", "Sprint started", sprint.getName() + " is now active");
        notificationService.notifyProjectMembers(sprint, sprint.getName() + " started");
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse completeSprint(Long projectId, Long sprintId) {
        Sprint sprint = findSprint(projectId, sprintId);
        sprint.setActive(false);
        sprint.setCompletedAt(Instant.now());
        auditEventService.record(projectId, "SPRINT_COMPLETED", "Sprint completed", sprint.getName() + " was completed");
        notificationService.notifyProjectMembers(sprint, sprint.getName() + " was completed");
        return toResponse(sprint);
    }

    @Transactional
    public void deleteSprint(Long projectId, Long sprintId) {
        Sprint sprint = findSprint(projectId, sprintId);
        issueRepository.findBySprintId(sprintId).forEach((issue) -> issue.setSprint(null));
        sprintRepository.delete(sprint);
        auditEventService.record(projectId, "SPRINT_DELETED", "Sprint deleted", sprint.getName() + " was removed");
    }

    public Sprint findSprint(Long projectId, Long sprintId) {
        projectService.findProject(projectId);
        return sprintRepository.findByProjectIdAndId(projectId, sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found: " + sprintId + " in project " + projectId));
    }

    public Sprint resolveSprint(Long projectId, Long sprintId) {
        if (sprintId == null) {
            return null;
        }
        return findSprint(projectId, sprintId);
    }

    private void deactivateActiveSprints(Long projectId) {
        sprintRepository.findByProjectIdAndActiveTrue(projectId)
                .forEach((sprint) -> sprint.setActive(false));
    }

    private SprintResponse toResponse(Sprint sprint) {
        return new SprintResponse(
                sprint.getId(),
                sprint.getProject().getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStartsAt(),
                sprint.getEndsAt(),
                determineStatus(sprint),
                sprint.isActive(),
                sprint.getCompletedAt(),
                sprint.getCreatedAt(),
                sprint.getIssues().size());
    }

    private SprintStatus determineStatus(Sprint sprint) {
        if (sprint.getCompletedAt() != null) {
            return SprintStatus.COMPLETED;
        }
        return sprint.isActive() ? SprintStatus.ACTIVE : SprintStatus.PLANNED;
    }
}
