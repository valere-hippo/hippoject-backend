package io.hippoject.backend.comment.service;

import io.hippoject.backend.comment.domain.Comment;
import io.hippoject.backend.comment.dto.CommentResponse;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getBody(),
                comment.getAuthorId(),
                comment.getCreatedAt());
    }
}
