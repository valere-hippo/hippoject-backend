package io.hippoject.backend.filter.repository;

import io.hippoject.backend.filter.domain.SavedFilter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedFilterRepository extends JpaRepository<SavedFilter, Long> {

    List<SavedFilter> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
}
