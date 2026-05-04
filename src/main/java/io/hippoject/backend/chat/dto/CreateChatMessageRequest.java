package io.hippoject.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatMessageRequest(
        @NotBlank(message = "Die Nachricht darf nicht leer sein")
        @Size(max = 2000, message = "Die Nachricht darf maximal 2000 Zeichen lang sein")
        String body) {
}
