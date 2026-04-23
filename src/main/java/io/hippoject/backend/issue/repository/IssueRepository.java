package io.hippoject.backend.issue.repository;

import io.hippoject.backend.issue.domain.Issue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    Optional<Issue> findByProjectIdAndId(Long projectId, Long id);

    long countByProjectId(Long projectId);
}
