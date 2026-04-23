package io.hippoject.backend.project.service;

import io.hippoject.backend.common.exception.ConflictException;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.dto.CreateProjectRequest;
import io.hippoject.backend.project.dto.ProjectResponse;
import io.hippoject.backend.project.repository.ProjectRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Jwt jwt) {
        String projectKey = request.key().trim().toUpperCase();
        if (projectRepository.existsByKeyIgnoreCase(projectKey)) {
            throw new ConflictException("Project key already exists: " + projectKey);
        }

        Project project = new Project(
                projectKey,
                request.name().trim(),
                request.description().trim(),
                actorId(jwt),
                Instant.now());

        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProject(Long projectId) {
        return toResponse(findProject(projectId));
    }

    public Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
    }

    ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                project.getCreatedAt(),
                project.getIssues().size());
    }

    private String actorId(Jwt jwt) {
        return jwt.getSubject() != null ? jwt.getSubject() : jwt.getClaimAsString("preferred_username");
    }
}
