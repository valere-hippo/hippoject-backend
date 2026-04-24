package io.hippoject.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Ein Kommentar ist erforderlich")
        @Size(max = 2000, message = "Ein Kommentar darf höchstens 2000 Zeichen lang sein")
        String body) {
}
