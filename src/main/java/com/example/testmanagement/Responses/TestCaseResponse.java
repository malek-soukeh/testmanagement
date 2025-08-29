package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestSuite;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestCaseResponse {
    private Long id;
    private String title;
    private String description;
    private String testSteps;
    private String expectedResult;
    private TestCase.TestType testType;
    private TestCase.Priority priority;
    private TestCase.Status status;
    private String createdBy;
    private TestSuiteInfo testSuite;
    private ProjectInfo project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static TestCaseResponse fromEntity(TestCase testCase) {
        TestCaseResponse response = new TestCaseResponse();
        response.setId(testCase.getId());
        response.setTitle(testCase.getTitle());
        response.setDescription(testCase.getDescription());
        response.setTestSteps(testCase.getTestSteps());
        response.setExpectedResult(testCase.getExpectedResult());
        response.setTestType(testCase.getTestType());
        response.setPriority(testCase.getPriority());
        response.setStatus(testCase.getStatus());
        response.setCreatedBy(testCase.getCreatedBy().getEmail());
        response.setCreatedAt(testCase.getCreatedAt());
        response.setUpdatedAt(testCase.getUpdatedAt());

        if(testCase.getTestSuite() != null) {
            response.setTestSuite(TestSuiteInfo.fromEntity(testCase.getTestSuite()));

            if(testCase.getTestSuite().getProject() != null) {
                response.setProject(ProjectInfo.fromEntity(testCase.getTestSuite().getProject()));
            }
        }
        return response;
    }
}


@Data
class TestSuiteInfo
{
    private Long id;
    private String suiteName;

    public static TestSuiteInfo fromEntity(TestSuite testSuite) {
        TestSuiteInfo testSuiteInfo = new TestSuiteInfo();
        testSuiteInfo.setId(testSuite.getId());
        testSuiteInfo.setSuiteName(testSuite.getSuiteName());
        return testSuiteInfo;
    }
}

@Data
class ProjectInfo
{
    private Long id;
    private String projectName;

    public static ProjectInfo fromEntity(Project testSuite) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setId(testSuite.getId());
        projectInfo.setProjectName(testSuite.getProjectName());
        return projectInfo;
    }
}