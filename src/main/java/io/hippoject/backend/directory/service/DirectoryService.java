package io.hippoject.backend.directory.service;

import io.hippoject.backend.directory.dto.DirectoryMemberResponse;
import io.hippoject.backend.directory.dto.DirectoryProjectResponse;
import io.hippoject.backend.project.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DirectoryService {

    private final ProjectRepository projectRepository;

    public DirectoryService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<DirectoryProjectResponse> getDirectory() {
        return projectRepository.findAll().stream()
                .filter((project) -> project.getDeletedAt() == null)
                .map((project) -> new DirectoryProjectResponse(
                        project.getId(),
                        project.getKey(),
                        project.getName(),
                        project.getMembers().stream()
                                .map((member) -> new DirectoryMemberResponse(
                                        member.getId(),
                                        member.getUserId(),
                                        member.getDisplayName(),
                                        member.getEmail(),
                                        member.getRole()))
                                .toList()))
                .toList();
    }
}
