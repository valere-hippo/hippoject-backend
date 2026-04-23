package io.hippoject.backend.dashboard.service;

import io.hippoject.backend.dashboard.dto.DashboardSummaryResponse;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import io.hippoject.backend.issue.repository.IssueRepository;
import io.hippoject.backend.project.repository.ProjectRepository;
import io.hippoject.backend.sprint.repository.SprintRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;

    public DashboardService(ProjectRepository projectRepository, IssueRepository issueRepository, SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintRepository = sprintRepository;
    }

    public DashboardSummaryResponse getSummary() {
        var activeProjects = projectRepository.findAll().stream()
                .filter((project) -> project.getDeletedAt() == null)
                .toList();

        return new DashboardSummaryResponse(
                activeProjects.size(),
                activeProjects.stream().flatMap((project) -> project.getIssues().stream()).filter((issue) -> issue.getDeletedAt() == null && issue.getStatus() != IssueStatus.DONE).count(),
                activeProjects.stream().flatMap((project) -> project.getIssues().stream()).filter((issue) -> issue.getDeletedAt() == null && List.of(IssueStatus.IN_PROGRESS, IssueStatus.IN_REVIEW).contains(issue.getStatus())).count(),
                activeProjects.stream().flatMap((project) -> project.getIssues().stream()).filter((issue) -> issue.getDeletedAt() == null && issue.getPriority() == IssuePriority.CRITICAL).count(),
                activeProjects.stream().flatMap((project) -> project.getIssues().stream()).map((issue) -> issue.getSprint()).filter((sprint) -> sprint != null && sprint.getDeletedAt() == null && sprint.isActive()).map((sprint) -> sprint.getId()).distinct().count(),
                activeProjects.stream().flatMap((project) -> project.getIssues().stream()).filter((issue) -> issue.getDeletedAt() == null && issue.getIssueType() == IssueType.EPIC).count());
    }
}
