package io.hippoject.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Comment body is required")
        @Size(max = 2000, message = "Comment body must be at most 2000 characters")
        String body) {
}
