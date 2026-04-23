package io.hippoject.backend.notification.service;

import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.comment.domain.Comment;
import io.hippoject.backend.notification.domain.Notification;
import io.hippoject.backend.notification.dto.NotificationResponse;
import io.hippoject.backend.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._-]+)");

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> listNotifications(Jwt jwt) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(actorId(jwt)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Jwt jwt) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, actorId(jwt))
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));
        notification.setRead(true);
        return toResponse(notification);
    }

    @Transactional
    public void createMentionNotifications(Comment comment) {
        Set<String> mentions = extractMentions(comment.getBody());
        mentions.stream()
                .filter((recipientId) -> !recipientId.equalsIgnoreCase(comment.getAuthorId()))
                .forEach((recipientId) -> notificationRepository.save(new Notification(
                        recipientId,
                        "MENTION",
                        comment.getIssue().getProject().getId(),
                        comment.getIssue().getId(),
                        comment.getAuthorId() + " mentioned you on " + comment.getIssue().getIssueKey(),
                        false,
                        Instant.now())));
    }

    private Set<String> extractMentions(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        java.util.LinkedHashSet<String> mentions = new java.util.LinkedHashSet<>();
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getProjectId(),
                notification.getIssueId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
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
