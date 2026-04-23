package io.hippoject.backend.audit.service;

import io.hippoject.backend.audit.domain.AuditEvent;
import io.hippoject.backend.audit.repository.AuditEventRepository;
import io.hippoject.backend.realtime.service.RealtimeEventService;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final RealtimeEventService realtimeEventService;

    public AuditEventService(AuditEventRepository auditEventRepository, RealtimeEventService realtimeEventService) {
        this.auditEventRepository = auditEventRepository;
        this.realtimeEventService = realtimeEventService;
    }

    @Transactional
    public void record(Long projectId, String type, String title, String detail) {
        auditEventRepository.save(new AuditEvent(projectId, type, title, detail, Instant.now()));
        realtimeEventService.broadcastProjectUpdated(projectId);
    }
}
