package io.hippoject.backend.project.api;

import io.hippoject.backend.project.dto.CreateProjectRequest;
import io.hippoject.backend.project.dto.ProjectResponse;
import io.hippoject.backend.project.dto.UpdateProjectRequest;
import io.hippoject.backend.project.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request, @AuthenticationPrincipal Jwt jwt) {
        return projectService.createProject(request, jwt);
    }

    @GetMapping
    public List<ProjectResponse> listProjects(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return projectService.listProjects(includeArchived);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public ProjectResponse updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public ProjectResponse archiveProject(@PathVariable Long projectId) {
        return projectService.archiveProject(projectId);
    }

    @PutMapping("/{projectId}/restore")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public ProjectResponse restoreProject(@PathVariable Long projectId) {
        return projectService.restoreProject(projectId);
    }
}
