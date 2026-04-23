package io.hippoject.backend.audit.repository;

import io.hippoject.backend.audit.domain.AuditEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByProjectIdOrderByOccurredAtDesc(Long projectId);
}
