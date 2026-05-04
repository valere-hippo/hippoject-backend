package io.hippoject.backend.chat.domain;

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
@Table(name = "chat_participants")
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(nullable = false, length = 120)
    private String userId;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    protected ChatParticipant() {
    }

    public ChatParticipant(ChatConversation conversation, String userId, String displayName, String avatarUrl, Instant joinedAt) {
        this.conversation = conversation;
        this.userId = userId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
