package io.hippoject.backend.realtime.config;

import io.hippoject.backend.realtime.handler.RealtimeWebSocketHandler;
import io.hippoject.backend.realtime.handler.RealtimeHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RealtimeWebSocketConfig implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler realtimeWebSocketHandler;
    private final RealtimeHandshakeInterceptor realtimeHandshakeInterceptor;

    public RealtimeWebSocketConfig(RealtimeWebSocketHandler realtimeWebSocketHandler, RealtimeHandshakeInterceptor realtimeHandshakeInterceptor) {
        this.realtimeWebSocketHandler = realtimeWebSocketHandler;
        this.realtimeHandshakeInterceptor = realtimeHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/ws/realtime")
                .addInterceptors(realtimeHandshakeInterceptor)
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}
