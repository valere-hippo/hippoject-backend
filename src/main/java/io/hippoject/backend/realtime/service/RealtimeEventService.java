package io.hippoject.backend.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class RealtimeEventService {

    private final ObjectMapper objectMapper;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public RealtimeEventService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
        send(emitter, "connected", "ready");
        return emitter;
    }

    public void registerSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void broadcastProjectUpdated(Long projectId) {
        broadcast("project-updated", Map.of("projectId", projectId));
    }

    public void broadcastNotificationsUpdated(String recipientId) {
        broadcast("notifications-updated", Map.of("recipientId", recipientId));
    }

    public void broadcastHeartbeat() {
        broadcast("heartbeat", Map.of("status", "ok"));
    }

    private void broadcast(String eventType, Object payload) {
        String jsonPayload = toJson(payload);
        emitters.forEach((emitter) -> send(emitter, eventType, jsonPayload));
        String message = toJson(Map.of("type", eventType, "payload", payload));
        sessions.forEach((session) -> send(session, message));
    }

    private void send(SseEmitter emitter, String eventType, String payload) {
        try {
            emitter.send(SseEmitter.event().name(eventType).data(payload));
        } catch (IOException ex) {
            emitter.complete();
            emitters.remove(emitter);
        }
    }

    private void send(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                sessions.remove(session);
            }
        } catch (IOException ex) {
            sessions.remove(session);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize realtime payload", ex);
        }
    }
}
