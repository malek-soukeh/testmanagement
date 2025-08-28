package com.example.testmanagement.Services;


import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.ProjectRepository;
import com.example.testmanagement.Requests.CreateProjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Project createProject(Project project, String username) {
        Optional<User> user = Optional.ofNullable(userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found")));
        user.ifPresent(project::setCreatedBy);
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, CreateProjectRequest projectDetails, String username) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project Not Found"));
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can updated only your own projects");
        }
        if (projectDetails.getProjectName() != null) {
            project.setProjectName(projectDetails.getProjectName());
        }
        if (projectDetails.getDescription() != null) {
            project.setDescription(projectDetails.getDescription());
        }

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects(String username) {
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));
        return projectRepository.findByCreatedBy(user);
    }

    public void deleteProject(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own projects");
        }

        projectRepository.delete(project);
    }

    public List<Project> searchProjectsByName(String name) {
        return projectRepository.findByProjectNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Project> getProjectsByUser(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return projectRepository.findByCreatedBy(user);
    }


    public Map<String, Object> getProjectStatistics(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("projectId", project.getId());
        stats.put("projectName", project.getProjectName());
        stats.put("createdAt", project.getCreatedAt());
        stats.put("updatedAt", project.getUpdatedAt());
        stats.put("createdBy", project.getCreatedBy().getEmail());

        stats.put("totalTestSuites", 0);
        stats.put("totalTestCases", 0);
        stats.put("totalTestRuns", 0);

        return stats;
    }
}
