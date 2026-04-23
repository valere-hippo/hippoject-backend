package io.hippoject.backend.directory.api;

import io.hippoject.backend.directory.dto.DirectoryProjectResponse;
import io.hippoject.backend.directory.service.DirectoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @GetMapping
    public List<DirectoryProjectResponse> getDirectory() {
        return directoryService.getDirectory();
    }
}
