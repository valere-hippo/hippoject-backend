package io.hippoject.backend.chat.repository;

import io.hippoject.backend.chat.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop100ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    Optional<ChatMessage> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
