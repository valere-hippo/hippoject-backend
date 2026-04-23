package io.hippoject.backend.activity.service;

import io.hippoject.backend.activity.dto.ProjectActivityItemResponse;
import io.hippoject.backend.project.domain.Project;
import io.hippoject.backend.project.service.ProjectService;
import io.hippoject.backend.sprint.repository.SprintRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectActivityService {

    private final ProjectService projectService;
    private final SprintRepository sprintRepository;

    public ProjectActivityService(ProjectService projectService, SprintRepository sprintRepository) {
        this.projectService = projectService;
        this.sprintRepository = sprintRepository;
    }

    public List<ProjectActivityItemResponse> listActivity(Long projectId) {
        Project project = projectService.findProject(projectId);
        List<ProjectActivityItemResponse> items = new ArrayList<>();

        items.add(new ProjectActivityItemResponse(
                "PROJECT_CREATED",
                "Project created",
                project.getName() + " was created by " + project.getOwnerId(),
                project.getCreatedAt()));

        project.getMembers().forEach((member) -> items.add(new ProjectActivityItemResponse(
                "MEMBER_ADDED",
                "Member added",
                member.getDisplayName() + " joined as " + member.getRole(),
                member.getAddedAt())));

        project.getIssues().forEach((issue) -> {
            items.add(new ProjectActivityItemResponse(
                    "ISSUE_CREATED",
                    "Issue created",
                    issue.getIssueKey() + " · " + issue.getTitle(),
                    issue.getCreatedAt()));

            if (!issue.getUpdatedAt().equals(issue.getCreatedAt())) {
                items.add(new ProjectActivityItemResponse(
                        "ISSUE_UPDATED",
                        "Issue updated",
                        issue.getIssueKey() + " moved to " + issue.getStatus(),
                        issue.getUpdatedAt()));
            }

            issue.getComments().forEach((comment) -> items.add(new ProjectActivityItemResponse(
                    "COMMENT_ADDED",
                    "Comment added",
                    comment.getAuthorId() + " commented on " + issue.getIssueKey(),
                    comment.getCreatedAt())));
        });

        sprintRepository.findByProjectIdOrderByStartsAtDesc(projectId).forEach((sprint) -> {
            items.add(new ProjectActivityItemResponse(
                    "SPRINT_CREATED",
                    "Sprint created",
                    sprint.getName() + " was planned",
                    sprint.getCreatedAt()));

            if (sprint.getCompletedAt() != null) {
                items.add(new ProjectActivityItemResponse(
                        "SPRINT_COMPLETED",
                        "Sprint completed",
                        sprint.getName() + " was completed",
                        sprint.getCompletedAt()));
            }
        });

        return items.stream()
                .sorted(Comparator.comparing(ProjectActivityItemResponse::occurredAt).reversed())
                .limit(40)
                .toList();
    }
}
