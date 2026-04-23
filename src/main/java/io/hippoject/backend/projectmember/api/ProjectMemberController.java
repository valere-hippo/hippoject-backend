package io.hippoject.backend.projectmember.api;

import io.hippoject.backend.projectmember.dto.CreateProjectMemberRequest;
import io.hippoject.backend.projectmember.dto.ProjectMemberResponse;
import io.hippoject.backend.projectmember.service.ProjectMemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public List<ProjectMemberResponse> listMembers(@PathVariable Long projectId) {
        return projectMemberService.listMembers(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public ProjectMemberResponse addMember(@PathVariable Long projectId, @Valid @RequestBody CreateProjectMemberRequest request) {
        return projectMemberService.addMember(projectId, request);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@roleGuard.hasAnyRole(authentication, 'hippoject-admin', 'project-admin', 'project-manager')")
    public void removeMember(@PathVariable Long projectId, @PathVariable Long memberId) {
        projectMemberService.removeMember(projectId, memberId);
    }
}
