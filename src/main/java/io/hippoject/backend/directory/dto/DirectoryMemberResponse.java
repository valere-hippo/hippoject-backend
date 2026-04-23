package io.hippoject.backend.directory.dto;

import io.hippoject.backend.projectmember.domain.ProjectRole;

public record DirectoryMemberResponse(
        Long id,
        String userId,
        String displayName,
        ProjectRole role) {
}
