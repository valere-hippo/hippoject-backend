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
        return new DashboardSummaryResponse(
                projectRepository.count(),
                issueRepository.countByStatusNot(IssueStatus.DONE),
                issueRepository.countByStatusIn(List.of(IssueStatus.IN_PROGRESS, IssueStatus.IN_REVIEW)),
                issueRepository.countByPriority(IssuePriority.CRITICAL),
                sprintRepository.countByActiveTrue(),
                issueRepository.countByIssueType(IssueType.EPIC));
    }
}
