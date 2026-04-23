package io.hippoject.backend.activity.api;

import io.hippoject.backend.activity.dto.ProjectActivityItemResponse;
import io.hippoject.backend.activity.service.ProjectActivityService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
public class ProjectActivityController {

    private final ProjectActivityService projectActivityService;

    public ProjectActivityController(ProjectActivityService projectActivityService) {
        this.projectActivityService = projectActivityService;
    }

    @GetMapping
    public List<ProjectActivityItemResponse> listActivity(@PathVariable Long projectId) {
        return projectActivityService.listActivity(projectId);
    }
}
