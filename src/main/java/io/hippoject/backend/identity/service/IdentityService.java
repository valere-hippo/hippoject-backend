package io.hippoject.backend.identity.service;

import io.hippoject.backend.common.exception.ConflictException;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.identity.dto.CreateIdentityUserRequest;
import io.hippoject.backend.identity.dto.IdentityUserResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

@Service
@Transactional(readOnly = true)
public class IdentityService {

    private static final List<String> INVITE_ACTIONS = List.of("VERIFY_EMAIL", "UPDATE_PASSWORD");

    private final RestClient restClient;
    private final String keycloakUrl;
    private final String realm;
    private final String adminRealm;
    private final String adminClientId;
    private final String adminUsername;
    private final String adminPassword;
    private final String frontendClientId;
    private final String frontendRedirectUri;

    public IdentityService(
            @Value("${app.keycloak.url}") String keycloakUrl,
            @Value("${app.keycloak.realm}") String realm,
            @Value("${app.keycloak.admin.realm}") String adminRealm,
            @Value("${app.keycloak.admin.client-id}") String adminClientId,
            @Value("${app.keycloak.admin.username}") String adminUsername,
            @Value("${app.keycloak.admin.password}") String adminPassword,
            @Value("${app.keycloak.frontend-client-id}") String frontendClientId,
            @Value("${app.keycloak.frontend-redirect-uri}") String frontendRedirectUri) {
        this.restClient = RestClient.create();
        this.keycloakUrl = trimTrailingSlash(keycloakUrl);
        this.realm = realm;
        this.adminRealm = adminRealm;
        this.adminClientId = adminClientId;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.frontendClientId = frontendClientId;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    public List<IdentityUserResponse> listUsers(String query) {
        String token = adminAccessToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users?max=200";
        if (query != null && !query.isBlank()) {
            url += "&search=" + UriUtils.encodeQueryParam(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
        }

        KeycloakUserRepresentation[] users = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeycloakUserRepresentation[].class);

        if (users == null) {
            return List.of();
        }

        return Arrays.stream(users)
                .filter((user) -> Boolean.TRUE.equals(user.enabled()))
                .map(this::toResponse)
                .sorted((left, right) -> left.displayName().compareToIgnoreCase(right.displayName()))
                .toList();
    }

    @Transactional
    public IdentityUserResponse inviteUser(CreateIdentityUserRequest request) {
        String token = adminAccessToken();
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateKeycloakUserRequest(
                            username,
                            email,
                            firstName,
                            lastName,
                            true,
                            false,
                            INVITE_ACTIONS))
                    .retrieve()
                    .toBodilessEntity();

            String userId = extractUserId(response.getHeaders().getFirst(HttpHeaders.LOCATION));
            try {
                sendInviteEmail(token, userId);
                return getUser(token, userId);
            } catch (RuntimeException exception) {
                deleteUser(token, userId);
                throw new IllegalStateException("Der Benutzer wurde angelegt, aber die Einladungs-E-Mail konnte nicht versendet werden", exception);
            }
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new ConflictException("Benutzer oder E-Mail existiert bereits in Keycloak");
            }
            throw exception;
        }
    }

    private void sendInviteEmail(String token, String userId) {
        restClient.put()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId
                        + "/execute-actions-email?client_id="
                        + UriUtils.encodeQueryParam(frontendClientId, java.nio.charset.StandardCharsets.UTF_8)
                        + "&redirect_uri="
                        + UriUtils.encodeQueryParam(frontendRedirectUri, java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(INVITE_ACTIONS)
                .retrieve()
                .toBodilessEntity();
    }

    private IdentityUserResponse getUser(String token, String userId) {
        KeycloakUserRepresentation user = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeycloakUserRepresentation.class);

        if (user == null) {
            throw new NotFoundException("Benutzer in Keycloak nicht gefunden: " + userId);
        }
        return toResponse(user);
    }

    private void deleteUser(String token, String userId) {
        restClient.delete()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .retrieve()
                .toBodilessEntity();
    }

    private IdentityUserResponse toResponse(KeycloakUserRepresentation user) {
        String firstName = trimToNull(user.firstName());
        String lastName = trimToNull(user.lastName());
        String displayName = firstName != null || lastName != null
                ? ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim()
                : user.username();

        return new IdentityUserResponse(
                user.id(),
                user.username(),
                trimToNull(user.email()),
                firstName,
                lastName,
                displayName,
                Boolean.TRUE.equals(user.emailVerified()),
                Boolean.TRUE.equals(user.enabled()));
    }

    private String adminAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", adminClientId);
        form.add("username", adminUsername);
        form.add("password", adminPassword);

        TokenResponse tokenResponse = restClient.post()
                .uri(keycloakUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("Keycloak-Admin-Token konnte nicht geladen werden");
        }
        return tokenResponse.accessToken();
    }

    private String extractUserId(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Keycloak hat keine Benutzer-ID zurückgegeben");
        }
        int index = location.lastIndexOf('/');
        if (index < 0 || index + 1 >= location.length()) {
            throw new IllegalStateException("Keycloak-Location ist ungültig: " + location);
        }
        return location.substring(index + 1);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record TokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }

    private record KeycloakUserRepresentation(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean emailVerified,
            Boolean enabled) {
    }

    private record CreateKeycloakUserRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            boolean emailVerified,
            List<String> requiredActions) {
    }
}
