package com.example.testmanagement.Services;


import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.ProjectRepository;
import com.example.testmanagement.Responses.ProjectResponse;
import org.mockito.InjectMocks;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Project updateProject(Long id, Project projectDetails, String username) {
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
        if (projectDetails.getStatus() != null) {
            project.setStatus(projectDetails.getStatus());
        }
        if (projectDetails.getTeamSize() != null) {
            project.setTeamSize(projectDetails.getTeamSize());
        }
        return projectRepository.save(project);
    }

    public List<ProjectResponse> getAllProjects(String username) {
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));
        List<Project> projects = projectRepository.findByCreatedBy(user);

        return projects.stream()
                .map(ProjectResponse::fromEntity)
                .toList();
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


    @Transactional(readOnly = true)
    public Optional<ProjectResponse> getProjectById(Long id) {
        return projectRepository.findById(id).map(ProjectResponse::fromEntity);
    }



    public Map<String, Object> getProjectStatistics(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("projectId", project.getId());
        stats.put("projectName", project.getProjectName());
        stats.put("createdAt", project.getCreatedAt());
        stats.put("updatedAt", project.getUpdatedAt());
        stats.put("createdBy", project.getCreatedBy().getEmail());

        long totalTestSuites = project.getTestSuites().size();
        stats.put("totalTestSuites", totalTestSuites);

        List<TestCase> allTestCases = project.getTestSuites().stream()
                .flatMap(suite -> suite.getTestCases().stream())
                .toList();
        long totalTestCases = allTestCases.size();
        stats.put("totalTestCases", totalTestCases);

        long passedTests = allTestCases.stream()
                .filter(tc -> {
                    List<TestResult> results = tc.getTestResults();
                    return !results.isEmpty() && results.get(results.size() - 1).getStatus() == TestResult.ResultStatus.PASSED;
                })
                .count();

        double passRate = totalTestCases == 0 ? 0.0 : (passedTests * 100.0) / totalTestCases;
        stats.put("passRate", passRate);

        long automatedTests = allTestCases.stream()
                .filter(tc -> tc.getTestType() == TestCase.TestType.AUTOMATED)
                .count();
        double automationCoverage = totalTestCases == 0 ? 0.0 : (automatedTests * 100.0) / totalTestCases;
        stats.put("automationCoverage", automationCoverage);

        long criticalBugs = allTestCases.stream()
                .flatMap(tc -> tc.getTestResults().stream())
                .filter(tr -> tr.getStatus() == TestResult.ResultStatus.FAILED )
                .count();
        stats.put("criticalBugs", criticalBugs);


        stats.put("totalTestSuites", totalTestSuites);
        stats.put("totalTestCases", totalTestCases);

        return stats;
    }


    }

