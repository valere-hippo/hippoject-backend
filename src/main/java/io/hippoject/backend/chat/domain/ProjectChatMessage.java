package io.hippoject.backend.chat.domain;

import io.hippoject.backend.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_chat_messages")
public class ProjectChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 120)
    private String authorId;

    @Column(nullable = false, length = 120)
    private String authorDisplayName;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ProjectChatMessage() {
    }

    public ProjectChatMessage(Project project, String authorId, String authorDisplayName, String body, Instant createdAt) {
        this.project = project;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
