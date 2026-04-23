package io.hippoject.backend.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String recipientId;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long issueId;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(String recipientId, String type, Long projectId, Long issueId, String message, boolean read, Instant createdAt) {
        this.recipientId = recipientId;
        this.type = type;
        this.projectId = projectId;
        this.issueId = issueId;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getType() {
        return type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
