package io.hippoject.backend.chat.service;

import io.hippoject.backend.chat.domain.ChatConversation;
import io.hippoject.backend.chat.domain.ChatMessage;
import io.hippoject.backend.chat.domain.ChatParticipant;
import io.hippoject.backend.chat.dto.ChatConversationResponse;
import io.hippoject.backend.chat.dto.ChatMessageResponse;
import io.hippoject.backend.chat.dto.ChatParticipantResponse;
import io.hippoject.backend.chat.dto.CreateChatConversationRequest;
import io.hippoject.backend.chat.dto.CreateChatMessageRequest;
import io.hippoject.backend.chat.repository.ChatConversationRepository;
import io.hippoject.backend.chat.repository.ChatMessageRepository;
import io.hippoject.backend.chat.repository.ChatParticipantRepository;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.identity.dto.IdentityUserResponse;
import io.hippoject.backend.identity.service.IdentityService;
import io.hippoject.backend.realtime.service.RealtimeEventService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatConversationRepository chatConversationRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final IdentityService identityService;
    private final RealtimeEventService realtimeEventService;

    public ChatService(
            ChatConversationRepository chatConversationRepository,
            ChatParticipantRepository chatParticipantRepository,
            ChatMessageRepository chatMessageRepository,
            IdentityService identityService,
            RealtimeEventService realtimeEventService) {
        this.chatConversationRepository = chatConversationRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.identityService = identityService;
        this.realtimeEventService = realtimeEventService;
    }

    public List<ChatConversationResponse> listConversations(Jwt jwt) {
        String actorId = actorId(jwt);
        return chatParticipantRepository.findByUserIdIgnoreCase(actorId).stream()
                .map((participant) -> participant.getConversation())
                .distinct()
                .sorted(Comparator.comparing(ChatConversation::getUpdatedAt).reversed())
                .map((conversation) -> toConversationResponse(conversation, actorId))
                .toList();
    }

    @Transactional
    public ChatConversationResponse createConversation(CreateChatConversationRequest request, Jwt jwt) {
        String actorId = actorId(jwt);
        Map<String, IdentityUserResponse> usersByUsername = loadUsersByUsername();
        List<String> participantIds = normalizeParticipants(actorId, request.participantUserIds());
        if (!request.groupChat() && participantIds.size() != 2) {
            throw new IllegalArgumentException("Ein direkter Chat braucht genau zwei Teilnehmer");
        }
        if (request.groupChat() && participantIds.size() < 2) {
            throw new IllegalArgumentException("Ein Gruppenchat braucht mindestens zwei Teilnehmer");
        }

        Instant now = Instant.now();
        ChatConversation conversation = chatConversationRepository.save(new ChatConversation(
                request.groupChat() ? trimToNull(request.title()) : null,
                request.groupChat(),
                actorId,
                now));

        for (String participantId : participantIds) {
            IdentityUserResponse user = usersByUsername.get(participantId.toLowerCase(Locale.ROOT));
            chatParticipantRepository.save(new ChatParticipant(
                    conversation,
                    participantId,
                    user != null ? user.displayName() : participantId,
                    user != null ? user.avatarUrl() : null,
                    now));
        }

        realtimeEventService.broadcastChatUpdated(conversation.getId(), participantIds);
        return toConversationResponse(conversation, actorId);
    }

    public List<ChatMessageResponse> listMessages(Long conversationId, Jwt jwt) {
        String actorId = actorId(jwt);
        ensureParticipant(conversationId, actorId);
        return chatMessageRepository.findTop100ByConversationIdOrderByCreatedAtDesc(conversationId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse createMessage(Long conversationId, CreateChatMessageRequest request, Jwt jwt) {
        String actorId = actorId(jwt);
        ChatParticipant actorParticipant = ensureParticipant(conversationId, actorId);
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Chat nicht gefunden: " + conversationId));
        Instant now = Instant.now();
        ChatMessage message = chatMessageRepository.save(new ChatMessage(
                conversation,
                actorId,
                actorParticipant.getDisplayName(),
                request.body().trim(),
                now));
        conversation.setUpdatedAt(now);
        realtimeEventService.broadcastChatUpdated(conversationId, participantIds(conversationId));
        return toMessageResponse(message);
    }

    private ChatConversationResponse toConversationResponse(ChatConversation conversation, String viewerId) {
        List<ChatParticipantResponse> participants = chatParticipantRepository.findByConversationIdOrderByDisplayNameAsc(conversation.getId()).stream()
                .map(this::toParticipantResponse)
                .toList();
        ChatMessage lastMessage = chatMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        String title = conversation.getTitle();
        if (title == null || title.isBlank()) {
            title = participants.stream()
                    .filter((participant) -> !participant.userId().equalsIgnoreCase(viewerId))
                    .map(ChatParticipantResponse::displayName)
                    .findFirst()
                    .orElse("Direkter Chat");
        }
        return new ChatConversationResponse(
                conversation.getId(),
                title,
                conversation.isGroupChat(),
                participants,
                lastMessage != null ? lastMessage.getBody() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : null,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }

    private ChatParticipantResponse toParticipantResponse(ChatParticipant participant) {
        return new ChatParticipantResponse(participant.getUserId(), participant.getDisplayName(), participant.getAvatarUrl());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getAuthorId(),
                message.getAuthorDisplayName(),
                message.getBody(),
                message.getCreatedAt());
    }

    private ChatParticipant ensureParticipant(Long conversationId, String userId) {
        return chatParticipantRepository.findByConversationIdAndUserIdIgnoreCase(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("Chat nicht gefunden oder kein Zugriff: " + conversationId));
    }

    private List<String> participantIds(Long conversationId) {
        return chatParticipantRepository.findByConversationIdOrderByDisplayNameAsc(conversationId).stream()
                .map(ChatParticipant::getUserId)
                .toList();
    }

    private Map<String, IdentityUserResponse> loadUsersByUsername() {
        Map<String, IdentityUserResponse> usersByUsername = new LinkedHashMap<>();
        identityService.listUsers(null).forEach((user) -> usersByUsername.put(user.username().toLowerCase(Locale.ROOT), user));
        return usersByUsername;
    }

    private List<String> normalizeParticipants(String actorId, List<String> requestedParticipants) {
        Map<String, String> participants = new LinkedHashMap<>();
        participants.put(actorId.toLowerCase(Locale.ROOT), actorId);
        if (requestedParticipants != null) {
            requestedParticipants.stream()
                    .map(this::trimToNull)
                    .filter(java.util.Objects::nonNull)
                    .forEach((participant) -> participants.put(participant.toLowerCase(Locale.ROOT), participant));
        }
        return new ArrayList<>(participants.values());
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
