package io.hippoject.backend.identity.service;

import io.hippoject.backend.common.exception.ConflictException;
import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.identity.dto.CreateIdentityUserRequest;
import io.hippoject.backend.identity.dto.IdentityUserResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    private static final List<String> MANAGEABLE_REALM_ROLES = List.of(
            "hippoject-admin",
            "project-admin",
            "project-manager",
            "developer",
            "reporter");

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
            url += "&search=" + UriUtils.encodeQueryParam(query.trim(), StandardCharsets.UTF_8);
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
                .map((user) -> toResponse(token, user))
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
                assignRealmRoles(token, userId, normalizeRealmRoles(request.realmRoles()));
                sendInviteEmail(token, userId);
                return getUser(token, userId);
            } catch (RuntimeException exception) {
                deleteUser(token, userId);
                throw new IllegalStateException("Der Benutzer wurde angelegt, aber Rollen oder Einladung konnten nicht vollständig eingerichtet werden", exception);
            }
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new ConflictException("Benutzer oder E-Mail existiert bereits in Keycloak");
            }
            throw exception;
        }
    }

    @Transactional
    public IdentityUserResponse updateUserRoles(String userId, List<String> realmRoles) {
        String token = adminAccessToken();
        assignRealmRoles(token, userId, normalizeRealmRoles(realmRoles));
        return getUser(token, userId);
    }

    private void sendInviteEmail(String token, String userId) {
        restClient.put()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId
                        + "/execute-actions-email?client_id="
                        + UriUtils.encodeQueryParam(frontendClientId, StandardCharsets.UTF_8)
                        + "&redirect_uri="
                        + UriUtils.encodeQueryParam(frontendRedirectUri, StandardCharsets.UTF_8))
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
        return toResponse(token, user);
    }

    private void deleteUser(String token, String userId) {
        restClient.delete()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .retrieve()
                .toBodilessEntity();
    }

    private IdentityUserResponse toResponse(String token, KeycloakUserRepresentation user) {
        String firstName = trimToNull(user.firstName());
        String lastName = trimToNull(user.lastName());
        String displayName = firstName != null || lastName != null
                ? ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim()
                : user.username();
        List<String> realmRoles = loadUserRealmRoles(token, user.id());

        return new IdentityUserResponse(
                user.id(),
                user.username(),
                trimToNull(user.email()),
                firstName,
                lastName,
                displayName,
                Boolean.TRUE.equals(user.emailVerified()),
                Boolean.TRUE.equals(user.enabled()),
                realmRoles);
    }

    private List<String> loadUserRealmRoles(String token, String userId) {
        KeycloakRoleRepresentation[] roles = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeycloakRoleRepresentation[].class);

        if (roles == null) {
            return List.of();
        }

        return Arrays.stream(roles)
                .map(KeycloakRoleRepresentation::name)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(MANAGEABLE_REALM_ROLES::contains)
                .sorted(Comparator.comparingInt(MANAGEABLE_REALM_ROLES::indexOf))
                .toList();
    }

    private void assignRealmRoles(String token, String userId, List<String> desiredRoleNames) {
        List<KeycloakRoleRepresentation> currentRoles = loadUserRealmRoleRepresentations(token, userId);
        List<KeycloakRoleRepresentation> currentManageableRoles = currentRoles.stream()
                .filter(role -> role.name() != null && MANAGEABLE_REALM_ROLES.contains(role.name()))
                .toList();

        List<KeycloakRoleRepresentation> rolesToRemove = currentManageableRoles.stream()
                .filter(role -> !desiredRoleNames.contains(role.name()))
                .toList();
        if (!rolesToRemove.isEmpty()) {
            restClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rolesToRemove)
                    .retrieve()
                    .toBodilessEntity();
        }

        List<String> currentRoleNames = currentManageableRoles.stream()
                .map(KeycloakRoleRepresentation::name)
                .filter(Objects::nonNull)
                .toList();
        List<KeycloakRoleRepresentation> rolesToAdd = desiredRoleNames.stream()
                .filter(roleName -> !currentRoleNames.contains(roleName))
                .map(roleName -> getRealmRole(token, roleName))
                .toList();
        if (!rolesToAdd.isEmpty()) {
            restClient.post()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rolesToAdd)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private List<KeycloakRoleRepresentation> loadUserRealmRoleRepresentations(String token, String userId) {
        KeycloakRoleRepresentation[] roles = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeycloakRoleRepresentation[].class);

        return roles == null ? List.of() : Arrays.asList(roles);
    }

    private KeycloakRoleRepresentation getRealmRole(String token, String roleName) {
        if (!MANAGEABLE_REALM_ROLES.contains(roleName)) {
            throw new IllegalArgumentException("Unbekannte Rolle: " + roleName);
        }

        KeycloakRoleRepresentation role = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/" + UriUtils.encodePathSegment(roleName, StandardCharsets.UTF_8))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeycloakRoleRepresentation.class);

        if (role == null || role.name() == null || role.id() == null) {
            throw new NotFoundException("Keycloak-Rolle nicht gefunden: " + roleName);
        }
        return role;
    }

    private List<String> normalizeRealmRoles(List<String> realmRoles) {
        if (realmRoles == null || realmRoles.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Rolle ist erforderlich");
        }

        List<String> normalizedRoles = realmRoles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(MANAGEABLE_REALM_ROLES::contains)
                .distinct()
                .sorted(Comparator.comparingInt(MANAGEABLE_REALM_ROLES::indexOf))
                .toList();

        if (normalizedRoles.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine gültige Rolle ist erforderlich");
        }

        return normalizedRoles;
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

    private record KeycloakRoleRepresentation(
            String id,
            String name,
            String description,
            boolean composite,
            boolean clientRole,
            String containerId) {
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
