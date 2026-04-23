package io.hippoject.backend.projectmember.repository;

import io.hippoject.backend.projectmember.domain.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectIdOrderByAddedAtAsc(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserIdIgnoreCase(Long projectId, String userId);

    Optional<ProjectMember> findByProjectIdAndId(Long projectId, Long id);
}
