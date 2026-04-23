package io.hippoject.backend.audit.service;

import io.hippoject.backend.audit.domain.AuditEvent;
import io.hippoject.backend.audit.repository.AuditEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void record(Long projectId, String type, String title, String detail) {
        auditEventRepository.save(new AuditEvent(projectId, type, title, detail, Instant.now()));
    }
}
