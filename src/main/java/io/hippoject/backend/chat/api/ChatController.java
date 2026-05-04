package io.hippoject.backend.chat.api;

import io.hippoject.backend.chat.dto.ChatConversationResponse;
import io.hippoject.backend.chat.dto.ChatMessageResponse;
import io.hippoject.backend.chat.dto.CreateChatConversationRequest;
import io.hippoject.backend.chat.dto.CreateChatMessageRequest;
import io.hippoject.backend.chat.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public List<ChatConversationResponse> listConversations(@AuthenticationPrincipal Jwt jwt) {
        return chatService.listConversations(jwt);
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatConversationResponse createConversation(@Valid @RequestBody CreateChatConversationRequest request, @AuthenticationPrincipal Jwt jwt) {
        return chatService.createConversation(request, jwt);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponse> listMessages(@PathVariable Long conversationId, @AuthenticationPrincipal Jwt jwt) {
        return chatService.listMessages(conversationId, jwt);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse createMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody CreateChatMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return chatService.createMessage(conversationId, request, jwt);
    }
}
