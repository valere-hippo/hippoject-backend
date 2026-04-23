package io.hippoject.backend.realtime.handler;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RealtimeHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    public RealtimeHandshakeInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!securityEnabled) {
            return true;
        }

        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            var jwt = jwtDecoder.decode(token);
            attributes.put("userId", jwt.getClaimAsString("preferred_username") != null ? jwt.getClaimAsString("preferred_username") : jwt.getSubject());
            return true;
        } catch (Exception ex) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
