package io.hippoject.backend.chat.service;

import io.hippoject.backend.chat.domain.ProjectChatMessage;
import io.hippoject.backend.chat.dto.CreateProjectChatMessageRequest;
import io.hippoject.backend.chat.dto.ProjectChatMessageResponse;
import io.hippoject.backend.chat.repository.ProjectChatMessageRepository;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.projectmember.domain.ProjectMember;
import io.hippoject.backend.projectmember.repository.ProjectMemberRepository;
import io.hippoject.backend.realtime.service.RealtimeEventService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectChatService {

    private final ProjectChatMessageRepository projectChatMessageRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final RealtimeEventService realtimeEventService;

    public ProjectChatService(
            ProjectChatMessageRepository projectChatMessageRepository,
            ProjectService projectService,
            ProjectMemberRepository projectMemberRepository,
            RealtimeEventService realtimeEventService) {
        this.projectChatMessageRepository = projectChatMessageRepository;
        this.projectService = projectService;
        this.projectMemberRepository = projectMemberRepository;
        this.realtimeEventService = realtimeEventService;
    }

    public List<ProjectChatMessageResponse> listMessages(Long projectId, Jwt jwt) {
        ensureProjectChatAccess(projectId, jwt);
        return projectChatMessageRepository.findTop100ByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .sorted(Comparator.comparing(ProjectChatMessage::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectChatMessageResponse createMessage(Long projectId, CreateProjectChatMessageRequest request, Jwt jwt) {
        Project project = projectService.findProject(projectId);
        ProjectMember authorMember = ensureProjectChatAccess(projectId, jwt);
        String authorId = actorId(jwt);
        String authorDisplayName = authorMember != null ? authorMember.getDisplayName() : authorId;
        ProjectChatMessage message = new ProjectChatMessage(
                project,
                authorId,
                authorDisplayName,
                request.body().trim(),
                Instant.now());
        ProjectChatMessage savedMessage = projectChatMessageRepository.save(message);
        realtimeEventService.broadcastProjectChatMessage(projectId);
        return toResponse(savedMessage);
    }

    private ProjectMember ensureProjectChatAccess(Long projectId, Jwt jwt) {
        projectService.findProject(projectId);
        if (jwt == null) {
            return null;
        }
        String actorId = actorId(jwt);
        return projectMemberRepository.findByProjectIdAndUserIdIgnoreCase(projectId, actorId)
                .orElseThrow(() -> new NotFoundException("Projektchat nicht gefunden oder kein Zugriff: " + projectId));
    }

    private ProjectChatMessageResponse toResponse(ProjectChatMessage message) {
        return new ProjectChatMessageResponse(
                message.getId(),
                message.getProject().getId(),
                message.getAuthorId(),
                message.getAuthorDisplayName(),
                message.getBody(),
                message.getCreatedAt());
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
