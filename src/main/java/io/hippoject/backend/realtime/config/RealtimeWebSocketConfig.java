package io.hippoject.backend.realtime.config;

import io.hippoject.backend.realtime.handler.RealtimeWebSocketHandler;
import io.hippoject.backend.realtime.handler.RealtimeHandshakeInterceptor;
import java.util.Arrays;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RealtimeWebSocketConfig implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler realtimeWebSocketHandler;
    private final RealtimeHandshakeInterceptor realtimeHandshakeInterceptor;
    private final String corsAllowedOriginPatterns;

    public RealtimeWebSocketConfig(
            RealtimeWebSocketHandler realtimeWebSocketHandler,
            RealtimeHandshakeInterceptor realtimeHandshakeInterceptor,
            @Value("${app.security.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String corsAllowedOriginPatterns) {
        this.realtimeWebSocketHandler = realtimeWebSocketHandler;
        this.realtimeHandshakeInterceptor = realtimeHandshakeInterceptor;
        this.corsAllowedOriginPatterns = corsAllowedOriginPatterns;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/ws/realtime")
                .addInterceptors(realtimeHandshakeInterceptor)
                .setAllowedOriginPatterns(Arrays.stream(corsAllowedOriginPatterns.split(","))
                        .map(String::trim)
                        .filter(pattern -> !pattern.isBlank())
                        .toArray(String[]::new));
    }
}
