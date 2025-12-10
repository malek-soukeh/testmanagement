package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.ProjectRepository;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Responses.ProjectResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.testmanagement.Entities.Role;
import java.util.*;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final TestCaseRepository testCaseRepository;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository, UserService userService,
            TestCaseRepository testCaseRepository, AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.testCaseRepository = testCaseRepository;
        this.auditLogService = auditLogService;
    }

    public Project createProject(Project project, String username) {
        Optional<User> user = Optional.ofNullable(
                userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found")));
        user.ifPresent(project::setCreatedBy);

        if (project.getAssignedToUserId() != null) {
            User assignee = userService.findById(project.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned User Not Found"));
            project.setAssignedTo(assignee);
        }

        Project savedProject = projectRepository.save(project);
        auditLogService.logAction("CREATE", "PROJECT", savedProject.getId().toString(),
                "Created project: " + savedProject.getProjectName());
        return savedProject;
    }

    public Project updateProject(Long id, Project projectDetails, String username) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project Not Found"));
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!project.getCreatedBy().getId().equals(user.getId())) {
            // Check if admin? Or prevent update. Existing code prevented update.
            // If admin, maybe allow? For now, stick to original restriction or allow
            // Project Lead?
            // "Tester can only execute". Admin creates.
            boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
            if (!isAdmin) {
                throw new RuntimeException("You can updated only your own projects");
            }
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
        if (projectDetails.getAssignedToUserId() != null) {
            User assignee = userService.findById(projectDetails.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned User Not Found"));
            project.setAssignedTo(assignee);
        }
        Project updatedProject = projectRepository.save(project);
        auditLogService.logAction("UPDATE", "PROJECT", updatedProject.getId().toString(),
                "Updated project: " + updatedProject.getProjectName());
        return updatedProject;
    }

    public List<ProjectResponse> getAllProjects(String username) {
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        List<Project> projects;
        if (isAdmin) {
            projects = projectRepository.findAll();
        } else {
            // Tester or other: See created by me OR assigned to me
            List<Project> created = projectRepository.findByCreatedBy(user);
            List<Project> assigned = projectRepository.findByAssignedTo(user);
            Set<Project> combined = new HashSet<>(created);
            combined.addAll(assigned);
            projects = new ArrayList<>(combined);
        }

        return projects.stream()
                .map(ProjectResponse::fromEntity)
                .toList();
    }

    public void deleteProject(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        if (!isAdmin && !project.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own projects");
        }

        auditLogService.logAction("DELETE", "PROJECT", id.toString(), "Deleted project: " + project.getProjectName());

        // Manually delete all test cases associated with this project's test suites
        // to avoid foreign key constraint violations
        List<TestCase> testCases = testCaseRepository.findAllByTestSuite_Project_Id(id);
        testCaseRepository.deleteAll(testCases);

        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectResponse> getProjectById(Long id) {
        return projectRepository.findById(id).map(ProjectResponse::fromEntity);
    }

    public Map<String, Object> getProjectStatistics(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("projectId", project.getId());
        stats.put("projectName", project.getProjectName());
        stats.put("createdAt", project.getCreatedAt());
        stats.put("updatedAt", project.getUpdatedAt());
        if (project.getCreatedBy() != null) {
            stats.put("createdBy", project.getCreatedBy().getEmail());
        } else {
            stats.put("createdBy", "Unknown");
        }

        long totalTestSuites = project.getTestSuites().size();
        stats.put("totalTestSuites", totalTestSuites);

        // Use repository to fetch test cases directly to avoid mapping issues
        List<TestCase> allTestCases = testCaseRepository.findAllByTestSuite_Project_Id(projectId);
        long totalTestCases = allTestCases.size();
        stats.put("totalTestCases", totalTestCases);

        long passedTests = allTestCases.stream()
                .filter(tc -> {
                    List<TestResult> results = tc.getTestResults();
                    return !results.isEmpty()
                            && results.get(results.size() - 1).getStatus() == TestResult.ResultStatus.PASSED;
                })
                .count();
        stats.put("passedTests", passedTests);

        double passRate = totalTestCases == 0 ? 0.0 : (passedTests * 100.0) / totalTestCases;
        stats.put("passRate", passRate);

        long automatedTests = allTestCases.stream()
                .filter(tc -> tc.getTestType() == TestCase.TestType.AUTOMATED)
                .count();
        double automationCoverage = totalTestCases == 0 ? 0.0 : (automatedTests * 100.0) / totalTestCases;
        stats.put("automationCoverage", automationCoverage);

        long criticalBugs = allTestCases.stream()
                .filter(tc -> {
                    List<TestResult> results = tc.getTestResults();
                    return !results.isEmpty()
                            && results.get(results.size() - 1).getStatus() == TestResult.ResultStatus.FAILED;
                })
                .count();
        stats.put("criticalBugs", criticalBugs);
        stats.put("failedTests", criticalBugs);

        stats.put("totalTestSuites", totalTestSuites);
        stats.put("totalTestCases", totalTestCases);

        return stats;
    }

    public Map<String, Object> getPortfolioStatistics(String username) {
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));

        // TEMPORARY FIX: Return all projects for everyone
        List<Project> projects = projectRepository.findAll();

        long totalProjects = projects.size();

        // Use repository to fetch all test cases
        List<TestCase> allTestCases = testCaseRepository.findAll();

        long totalTestCases = allTestCases.size();

        long totalPassedTests = allTestCases.stream()
                .filter(tc -> {
                    List<TestResult> results = tc.getTestResults();
                    return !results.isEmpty()
                            && results.get(results.size() - 1).getStatus() == TestResult.ResultStatus.PASSED;
                })
                .count();

        long totalCriticalIssues = allTestCases.stream()
                .filter(tc -> {
                    List<TestResult> results = tc.getTestResults();
                    return !results.isEmpty()
                            && results.get(results.size() - 1).getStatus() == TestResult.ResultStatus.FAILED;
                })
                .count();

        double overallPassRate = totalTestCases == 0 ? 0.0 : (totalPassedTests * 100.0) / totalTestCases;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", totalProjects);
        stats.put("totalTestCases", totalTestCases);
        stats.put("overallPassRate", overallPassRate);
        stats.put("criticalIssues", totalCriticalIssues);

        return stats;
    }
}
