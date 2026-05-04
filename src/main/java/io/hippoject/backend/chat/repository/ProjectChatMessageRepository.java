package io.hippoject.backend.chat.repository;

import io.hippoject.backend.chat.domain.ProjectChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectChatMessageRepository extends JpaRepository<ProjectChatMessage, Long> {

    List<ProjectChatMessage> findTop100ByProjectIdOrderByCreatedAtDesc(Long projectId);
}
