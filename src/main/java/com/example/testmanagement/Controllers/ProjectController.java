package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Responses.ProjectResponse;
import com.example.testmanagement.Services.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project, @AuthenticationPrincipal UserDetails userDetails) {
        Project createdProject = projectService.createProject(project, userDetails.getUsername());
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getAllProjects(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id , @AuthenticationPrincipal UserDetails userDetails) {
        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody Project projectDetails,
            @AuthenticationPrincipal UserDetails userDetails) {
        Project updatedProject = projectService.updateProject(id , projectDetails, userDetails.getUsername());
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping("/{projectId}/statistics")
    public ResponseEntity<Map<String, Object>> getProjectStatistics(@PathVariable Long projectId) {
        Map<String, Object> stats = projectService.getProjectStatistics(projectId);
        return ResponseEntity.ok(stats);
    }

}
