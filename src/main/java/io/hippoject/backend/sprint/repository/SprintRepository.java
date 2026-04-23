package io.hippoject.backend.sprint.repository;

import io.hippoject.backend.sprint.domain.Sprint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectIdOrderByStartsAtDesc(Long projectId);

    Optional<Sprint> findByProjectIdAndId(Long projectId, Long id);

    List<Sprint> findByProjectIdAndActiveTrue(Long projectId);

    long countByActiveTrue();
}
