package io.hippoject.backend.chat.repository;

import io.hippoject.backend.chat.domain.ChatParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByUserIdIgnoreCase(String userId);

    List<ChatParticipant> findByConversationIdOrderByDisplayNameAsc(Long conversationId);

    Optional<ChatParticipant> findByConversationIdAndUserIdIgnoreCase(Long conversationId, String userId);
}
