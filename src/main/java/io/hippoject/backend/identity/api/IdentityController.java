package io.hippoject.backend.identity.api;

import io.hippoject.backend.identity.dto.CreateIdentityUserRequest;
import io.hippoject.backend.identity.dto.IdentityUserResponse;
import io.hippoject.backend.identity.dto.UpdateIdentityProfileRequest;
import io.hippoject.backend.identity.dto.UpdateIdentityUserRolesRequest;
import io.hippoject.backend.identity.service.IdentityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/identity/users")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager', 'developer', 'reporter')")
    public List<IdentityUserResponse> listUsers(@RequestParam(required = false) String query) {
        return identityService.listUsers(query);
    }

    @GetMapping("/me")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager', 'developer', 'reporter')")
    public IdentityUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return identityService.currentUser(jwt);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public IdentityUserResponse inviteUser(@Valid @RequestBody CreateIdentityUserRequest request) {
        return identityService.inviteUser(request);
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public IdentityUserResponse updateUserRoles(@PathVariable String userId, @Valid @RequestBody UpdateIdentityUserRolesRequest request) {
        return identityService.updateUserRoles(userId, request.realmRoles());
    }

    @PutMapping("/me/profile")
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager', 'developer', 'reporter')")
    public IdentityUserResponse updateCurrentProfile(@Valid @RequestBody UpdateIdentityProfileRequest request, @AuthenticationPrincipal Jwt jwt) {
        return identityService.updateCurrentUserProfile(request.avatarUrl(), jwt);
    }
}
