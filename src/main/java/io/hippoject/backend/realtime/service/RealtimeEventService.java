package io.hippoject.backend.realtime.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class RealtimeEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
        send(emitter, "connected", "ready");
        return emitter;
    }

    public void broadcast(String eventType, String payload) {
        emitters.forEach((emitter) -> send(emitter, eventType, payload));
    }

    private void send(SseEmitter emitter, String eventType, String payload) {
        try {
            emitter.send(SseEmitter.event().name(eventType).data(payload));
        } catch (IOException ex) {
            emitter.complete();
            emitters.remove(emitter);
        }
    }
}
