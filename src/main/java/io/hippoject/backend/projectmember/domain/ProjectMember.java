package io.hippoject.backend.projectmember.domain;

import io.hippoject.backend.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 120)
    private String userId;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectRole role;

    @Column(nullable = false, updatable = false)
    private Instant addedAt;

    protected ProjectMember() {
    }

    public ProjectMember(Project project, String userId, String displayName, String email, ProjectRole role, Instant addedAt) {
        this.project = project;
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
