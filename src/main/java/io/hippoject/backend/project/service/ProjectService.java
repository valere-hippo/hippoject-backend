package io.hippoject.backend.project.service;

import io.hippoject.backend.audit.service.AuditEventService;
import io.hippoject.backend.common.exception.ConflictException;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.dto.CreateProjectRequest;
import io.hippoject.backend.project.dto.ProjectResponse;
import io.hippoject.backend.project.dto.UpdateProjectRequest;
import io.hippoject.backend.project.repository.ProjectRepository;
import io.hippoject.backend.projectmember.domain.ProjectMember;
import io.hippoject.backend.projectmember.domain.ProjectRole;
import io.hippoject.backend.projectmember.repository.ProjectMemberRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuditEventService auditEventService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, AuditEventService auditEventService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Jwt jwt) {
        String projectKey = request.key().trim().toUpperCase();
        if (projectRepository.existsByKeyIgnoreCase(projectKey)) {
            throw new ConflictException("Project key already exists: " + projectKey);
        }

        String actorId = actorId(jwt);
        Project project = new Project(
                projectKey,
                request.name().trim(),
                request.description().trim(),
                actorId,
                Instant.now());
        Project savedProject = projectRepository.save(project);
        ProjectMember ownerMember = new ProjectMember(savedProject, actorId, actorId, null, ProjectRole.PROJECT_ADMIN, Instant.now());
        savedProject.getMembers().add(ownerMember);
        projectMemberRepository.save(ownerMember);
        auditEventService.record(savedProject.getId(), "PROJECT_CREATED", "Project created", savedProject.getName() + " was created by " + actorId);
        return toResponse(savedProject);
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProject(Long projectId) {
        return toResponse(findProject(projectId));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = findProject(projectId);
        project.setName(request.name().trim());
        project.setDescription(request.description().trim());
        auditEventService.record(project.getId(), "PROJECT_UPDATED", "Project updated", project.getName() + " project settings were updated");
        return toResponse(project);
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
                project.getIssues().size(),
                project.getMembers().size());
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
}
