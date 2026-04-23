package io.hippoject.backend.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("roleGuard")
public class RoleGuard {

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    public boolean hasAnyRole(Authentication authentication, String... roles) {
        if (!securityEnabled) {
            return true;
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> wanted = Arrays.stream(roles)
                .map((role) -> "ROLE_" + role.toUpperCase())
                .collect(Collectors.toSet());

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(wanted::contains);
    }
}
