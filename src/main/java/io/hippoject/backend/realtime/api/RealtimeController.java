package io.hippoject.backend.realtime.api;

import io.hippoject.backend.realtime.service.RealtimeEventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {

    private final RealtimeEventService realtimeEventService;

    public RealtimeController(RealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    @GetMapping("/events")
    public SseEmitter events() {
        return realtimeEventService.subscribe();
    }
}
