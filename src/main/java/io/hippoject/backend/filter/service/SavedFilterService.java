package io.hippoject.backend.filter.service;

import io.hippoject.backend.common.exception.NotFoundException;
import io.hippoject.backend.filter.domain.SavedFilter;
import io.hippoject.backend.filter.dto.CreateSavedFilterRequest;
import io.hippoject.backend.filter.dto.SavedFilterResponse;
import io.hippoject.backend.filter.repository.SavedFilterRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SavedFilterService {

    private final SavedFilterRepository savedFilterRepository;

    public SavedFilterService(SavedFilterRepository savedFilterRepository) {
        this.savedFilterRepository = savedFilterRepository;
    }

    public List<SavedFilterResponse> listSavedFilters(Jwt jwt) {
        return savedFilterRepository.findByOwnerIdOrderByCreatedAtDesc(actorId(jwt)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedFilterResponse createSavedFilter(CreateSavedFilterRequest request, Jwt jwt) {
        SavedFilter savedFilter = new SavedFilter(
                actorId(jwt),
                request.name().trim(),
                trimToNull(request.query()),
                request.projectId(),
                request.status(),
                request.issueType(),
                request.priority(),
                trimToNull(request.assigneeId()),
                trimToNull(request.label()),
                Instant.now());
        return toResponse(savedFilterRepository.save(savedFilter));
    }

    @Transactional
    public void deleteSavedFilter(Long filterId, Jwt jwt) {
        SavedFilter savedFilter = savedFilterRepository.findByIdAndOwnerId(filterId, actorId(jwt))
                .orElseThrow(() -> new NotFoundException("Gespeicherter Filter nicht gefunden: " + filterId));
        savedFilterRepository.delete(savedFilter);
    }

    private SavedFilterResponse toResponse(SavedFilter savedFilter) {
        return new SavedFilterResponse(
                savedFilter.getId(),
                savedFilter.getName(),
                savedFilter.getQuery(),
                savedFilter.getProjectId(),
                savedFilter.getStatus(),
                savedFilter.getIssueType(),
                savedFilter.getPriority(),
                savedFilter.getAssigneeId(),
                savedFilter.getLabel(),
                savedFilter.getCreatedAt());
    }

    private String actorId(Jwt jwt) {
        if (jwt == null) {
            return "local-dev";
        }
        if (jwt.getClaimAsString("preferred_username") != null) {
            return jwt.getClaimAsString("preferred_username");
        }
        return jwt.getSubject() != null ? jwt.getSubject() : "local-dev";
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
