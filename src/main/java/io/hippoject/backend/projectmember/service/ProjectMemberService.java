package io.hippoject.backend.projectmember.service;

import io.hippoject.backend.audit.service.AuditEventService;
import io.hippoject.backend.common.exception.ConflictException;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.projectmember.domain.ProjectMember;
import io.hippoject.backend.projectmember.dto.CreateProjectMemberRequest;
import io.hippoject.backend.projectmember.dto.ProjectMemberResponse;
import io.hippoject.backend.projectmember.repository.ProjectMemberRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final AuditEventService auditEventService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository, ProjectService projectService, AuditEventService auditEventService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectService = projectService;
        this.auditEventService = auditEventService;
    }

    public List<ProjectMemberResponse> listMembers(Long projectId) {
        projectService.findProject(projectId);
        return projectMemberRepository.findByProjectIdOrderByAddedAtAsc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, CreateProjectMemberRequest request) {
        Project project = projectService.findProject(projectId);
        String userId = request.userId().trim();
        if (projectMemberRepository.findByProjectIdAndUserIdIgnoreCase(projectId, userId).isPresent()) {
            throw new ConflictException("Project member already exists: " + userId + " in project " + projectId);
        }

        ProjectMember member = new ProjectMember(
                project,
                userId,
                request.displayName().trim(),
                request.role(),
                Instant.now());
        ProjectMember savedMember = projectMemberRepository.save(member);
        auditEventService.record(projectId, "MEMBER_ADDED", "Member added", savedMember.getDisplayName() + " joined as " + savedMember.getRole());
        return toResponse(savedMember);
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUserId(),
                member.getDisplayName(),
                member.getRole(),
                member.getAddedAt());
    }
}
