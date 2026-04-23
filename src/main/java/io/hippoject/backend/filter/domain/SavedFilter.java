package io.hippoject.backend.filter.domain;

import io.hippoject.backend.issue.domain.IssuePriority;
import io.hippoject.backend.issue.domain.IssueStatus;
import io.hippoject.backend.issue.domain.IssueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "saved_filters")
public class SavedFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String ownerId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 200)
    private String query;

    @Column
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssuePriority priority;

    @Column(length = 120)
    private String assigneeId;

    @Column(length = 50)
    private String label;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected SavedFilter() {
    }

    public SavedFilter(String ownerId, String name, String query, Long projectId, IssueStatus status, IssueType issueType, IssuePriority priority, String assigneeId, String label, Instant createdAt) {
        this.ownerId = ownerId;
        this.name = name;
        this.query = query;
        this.projectId = projectId;
        this.status = status;
        this.issueType = issueType;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.label = label;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public Long getProjectId() {
        return projectId;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public String getLabel() {
        return label;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
