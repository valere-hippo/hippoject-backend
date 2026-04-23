package io.hippoject.backend.sprint.service;

import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.sprint.domain.Sprint;
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

    public SprintService(SprintRepository sprintRepository, ProjectService projectService) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
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
                Instant.now());
        return toResponse(sprintRepository.save(sprint));
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
                sprint.isActive(),
                sprint.getCreatedAt(),
                sprint.getIssues().size());
    }
}
