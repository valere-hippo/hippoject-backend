package io.hippoject.backend.activity.service;

import io.hippoject.backend.activity.dto.ProjectActivityItemResponse;
import io.hippoject.backend.audit.repository.AuditEventRepository;
import io.hippoject.backend.project.service.ProjectService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectActivityService {

    private final ProjectService projectService;
    private final AuditEventRepository auditEventRepository;

    public ProjectActivityService(ProjectService projectService, AuditEventRepository auditEventRepository) {
        this.projectService = projectService;
        this.auditEventRepository = auditEventRepository;
    }

    public List<ProjectActivityItemResponse> listActivity(Long projectId) {
        projectService.findProject(projectId);
        return auditEventRepository.findByProjectIdOrderByOccurredAtDesc(projectId).stream()
                .map((event) -> new ProjectActivityItemResponse(event.getType(), event.getTitle(), event.getDetail(), event.getOccurredAt()))
                .limit(60)
                .toList();
    }
}
