package io.hippoject.backend.chat.repository;

import io.hippoject.backend.chat.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
}
