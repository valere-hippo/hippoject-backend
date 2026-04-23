package io.hippoject.backend.filter.api;

import io.hippoject.backend.filter.dto.CreateSavedFilterRequest;
import io.hippoject.backend.filter.dto.SavedFilterResponse;
import io.hippoject.backend.filter.service.SavedFilterService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/filters")
public class SavedFilterController {

    private final SavedFilterService savedFilterService;

    public SavedFilterController(SavedFilterService savedFilterService) {
        this.savedFilterService = savedFilterService;
    }

    @GetMapping
    public List<SavedFilterResponse> listSavedFilters(@AuthenticationPrincipal Jwt jwt) {
        return savedFilterService.listSavedFilters(jwt);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedFilterResponse createSavedFilter(@Valid @RequestBody CreateSavedFilterRequest request, @AuthenticationPrincipal Jwt jwt) {
        return savedFilterService.createSavedFilter(request, jwt);
    }

    @DeleteMapping("/{filterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSavedFilter(@PathVariable Long filterId, @AuthenticationPrincipal Jwt jwt) {
        savedFilterService.deleteSavedFilter(filterId, jwt);
    }
}
