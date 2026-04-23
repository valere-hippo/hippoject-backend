package io.hippoject.backend.realtime.handler;

import io.hippoject.backend.realtime.service.RealtimeEventService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeEventService realtimeEventService;

    public RealtimeWebSocketHandler(RealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        realtimeEventService.registerSession(session);
        session.sendMessage(new TextMessage("{\"type\":\"connected\",\"payload\":\"ready\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        realtimeEventService.unregisterSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().contains("ping")) {
            session.sendMessage(new TextMessage("{\"type\":\"heartbeat\",\"payload\":{\"status\":\"ok\"}}"));
        }
    }
}
