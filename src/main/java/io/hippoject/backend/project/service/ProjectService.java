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
            throw new ConflictException("Der Projektschlüssel existiert bereits: " + projectKey);
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
        auditEventService.record(savedProject.getId(), "PROJECT_CREATED", "Projekt erstellt", savedProject.getName() + " wurde von " + actorId + " erstellt");
        return toResponse(savedProject);
    }

    public List<ProjectResponse> listProjects(boolean includeArchived) {
        return projectRepository.findAll().stream()
                .filter((project) -> includeArchived || project.getDeletedAt() == null)
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
        auditEventService.record(project.getId(), "PROJECT_UPDATED", "Projekt aktualisiert", "Die Projekteinstellungen von " + project.getName() + " wurden aktualisiert");
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse archiveProject(Long projectId) {
        Project project = findProject(projectId);
        project.setDeletedAt(Instant.now());
        auditEventService.record(project.getId(), "PROJECT_ARCHIVED", "Projekt archiviert", project.getName() + " wurde archiviert");
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse restoreProject(Long projectId) {
        Project project = findProjectIncludingArchived(projectId);
        project.setDeletedAt(null);
        auditEventService.record(project.getId(), "PROJECT_RESTORED", "Projekt wiederhergestellt", project.getName() + " wurde wiederhergestellt");
        return toResponse(project);
    }

    public Project findProject(Long projectId) {
        Project project = findProjectIncludingArchived(projectId);
        if (project.getDeletedAt() != null) {
            throw new NotFoundException("Projekt nicht gefunden: " + projectId);
        }
        return project;
    }

    public Project findProjectIncludingArchived(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projekt nicht gefunden: " + projectId));
    }

    ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                project.getCreatedAt(),
                project.getDeletedAt(),
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
