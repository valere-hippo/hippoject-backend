package io.hippoject.backend.chat.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateChatConversationRequest(
        @Size(max = 160, message = "Der Gruppenname darf maximal 160 Zeichen lang sein")
        String title,
        boolean groupChat,
        List<String> participantUserIds) {
}
