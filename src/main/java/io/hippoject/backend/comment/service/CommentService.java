package io.hippoject.backend.comment.service;

import io.hippoject.backend.audit.service.AuditEventService;
import io.hippoject.backend.comment.domain.Comment;
import io.hippoject.backend.comment.dto.CommentResponse;
import io.hippoject.backend.comment.dto.CreateCommentRequest;
import io.hippoject.backend.comment.repository.CommentRepository;
import io.hippoject.backend.issue.domain.Issue;
import io.hippoject.backend.issue.service.IssueService;
import io.hippoject.backend.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final AuditEventService auditEventService;

    public CommentService(CommentRepository commentRepository, IssueService issueService, CommentMapper commentMapper, NotificationService notificationService, AuditEventService auditEventService) {
        this.commentRepository = commentRepository;
        this.issueService = issueService;
        this.commentMapper = commentMapper;
        this.notificationService = notificationService;
        this.auditEventService = auditEventService;
    }

    public List<CommentResponse> listComments(Long projectId, Long issueId) {
        issueService.findIssue(projectId, issueId);
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long projectId, Long issueId, CreateCommentRequest request, Jwt jwt) {
        Issue issue = issueService.findIssue(projectId, issueId);
        Comment comment = new Comment(issue, request.body().trim(), actorId(jwt), Instant.now());
        Comment savedComment = commentRepository.save(comment);
        notificationService.createMentionNotifications(savedComment);
        auditEventService.record(projectId, "COMMENT_ADDED", "Kommentar hinzugefügt", savedComment.getAuthorId() + " hat " + issue.getIssueKey() + " kommentiert");
        return commentMapper.toResponse(savedComment);
    }

    private String actorId(Jwt jwt) {
        if (jwt == null) {
            return "local-dev";
        }
        if (jwt.getClaimAsString("preferred_username") != null) {
            return jwt.getClaimAsString("preferred_username");
        }
        return jwt.getSubject() != null ? jwt.getSubject() : "local-dev";
    }
}
