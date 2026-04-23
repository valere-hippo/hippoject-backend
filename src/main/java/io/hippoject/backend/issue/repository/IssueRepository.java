package io.hippoject.backend.issue.repository;

import io.hippoject.backend.issue.domain.Issue;
import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Issue> findAllByOrderByUpdatedAtDesc();

    Optional<Issue> findByProjectIdAndId(Long projectId, Long id);

    long countByProjectId(Long projectId);

    long countByStatusNot(IssueStatus status);

    long countByStatusIn(List<IssueStatus> statuses);

    long countByPriority(IssuePriority priority);

    long countByIssueType(IssueType issueType);

    long countByEpicId(Long epicId);

    long countByEpicIdAndStatus(Long epicId, IssueStatus status);

    List<Issue> findByEpicId(Long epicId);

    List<Issue> findBySprintId(Long sprintId);
}
