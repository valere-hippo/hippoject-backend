package io.hippoject.backend.chat.dto;

public record ChatParticipantResponse(
        String userId,
        String displayName,
        String avatarUrl) {
}
