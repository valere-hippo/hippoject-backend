package io.hippoject.backend.chat.api;

import io.hippoject.backend.chat.dto.CreateProjectChatMessageRequest;
import io.hippoject.backend.chat.dto.ProjectChatMessageResponse;
import io.hippoject.backend.chat.service.ProjectChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/chat/messages")
public class ProjectChatController {

    private final ProjectChatService projectChatService;

    public ProjectChatController(ProjectChatService projectChatService) {
        this.projectChatService = projectChatService;
    }

    @GetMapping
    public List<ProjectChatMessageResponse> listMessages(@PathVariable Long projectId, @AuthenticationPrincipal Jwt jwt) {
        return projectChatService.listMessages(projectId, jwt);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectChatMessageResponse createMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectChatMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return projectChatService.createMessage(projectId, request, jwt);
    }
}
