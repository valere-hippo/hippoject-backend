package io.hippoject.backend.sprint.api;

import io.hippoject.backend.sprint.dto.CreateSprintRequest;
import io.hippoject.backend.sprint.dto.SprintResponse;
import io.hippoject.backend.sprint.service.SprintService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    public List<SprintResponse> listSprints(@PathVariable Long projectId) {
        return sprintService.listSprints(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public SprintResponse createSprint(@PathVariable Long projectId, @Valid @RequestBody CreateSprintRequest request) {
        return sprintService.createSprint(projectId, request);
    }

    @PutMapping("/{sprintId}/start")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public SprintResponse startSprint(@PathVariable Long projectId, @PathVariable Long sprintId) {
        return sprintService.startSprint(projectId, sprintId);
    }

    @PutMapping("/{sprintId}/complete")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public SprintResponse completeSprint(@PathVariable Long projectId, @PathVariable Long sprintId) {
        return sprintService.completeSprint(projectId, sprintId);
    }
}
