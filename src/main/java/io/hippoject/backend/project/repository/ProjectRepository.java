package io.hippoject.backend.project.repository;

import io.hippoject.backend.project.domain.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByKeyIgnoreCase(String key);

    Optional<Project> findByKeyIgnoreCase(String key);
}
