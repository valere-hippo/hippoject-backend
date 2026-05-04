package io.hippoject.backend.chat.dto;

import java.time.Instant;
import java.util.List;

public record ChatConversationResponse(
        Long id,
        String title,
        boolean groupChat,
        List<ChatParticipantResponse> participants,
        String lastMessage,
        Instant lastMessageAt,
        Instant createdAt,
        Instant updatedAt) {
}
