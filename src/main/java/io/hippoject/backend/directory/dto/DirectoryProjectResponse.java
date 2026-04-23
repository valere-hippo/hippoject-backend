package io.hippoject.backend.directory.dto;

import java.util.List;

public record DirectoryProjectResponse(
        Long projectId,
        String projectKey,
        String projectName,
        List<DirectoryMemberResponse> members) {
}
