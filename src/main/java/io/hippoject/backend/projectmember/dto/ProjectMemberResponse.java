package io.hippoject.backend.projectmember.dto;

import io.hippoject.backend.projectmember.domain.ProjectRole;
import java.time.Instant;

public record ProjectMemberResponse(
        Long id,
        Long projectId,
        String userId,
        String displayName,
        ProjectRole role,
        Instant addedAt) {
}
