package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestSuite;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectResponse {
    private Long id;
    private String projectName;
    private String description;
    private String createdBy;
    private Project.Status status;
    private Long teamSize;
    private Long passRate;
    private Long criticalBugs;
    private Long automationCoverage;
    private Long totalTestSuites;
    private Long totalTestCases;
    private List<TestSuite> testSuites;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectName(project.getProjectName());
        response.setDescription(project.getDescription());
        response.setCreatedBy(project.getCreatedBy().getFirstName() + " " + project.getCreatedBy().getLastName());
        response.setStatus(project.getStatus());
        response.setTeamSize(project.getTeamSize());
        response.setLastActivity(project.getLastActivity());
        response.setTotalTestSuites(project.getTotalTestSuites());
        response.setTotalTestCases(project.getTotalTestCases());
        response.setTestSuites(project.getTestSuites());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}