package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestCaseStep;
import com.example.testmanagement.Entities.TestSuite;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class TestCaseResponse {
    private Long id;
    private String title;
    private String description;
    private String expectedResult;
    private String precondition;
    private TestCase.TestType testType;
    private TestCase.Priority priority;
    private TestCase.Status status;
    private String createdBy;
    private TestSuiteInfo testSuite;
    private ProjectInfo project;
    private List<TestCaseStepResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static TestCaseResponse fromEntity(TestCase testCase) {
        TestCaseResponse response = new TestCaseResponse();
        response.setId(testCase.getId());
        response.setTitle(testCase.getTitle());
        response.setDescription(testCase.getDescription());
        response.setPrecondition(testCase.getPrecondition());
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

            if(testCase.getTestCaseSteps() != null) {
                response.setSteps(testCase.getTestCaseSteps().stream().map(TestCaseStepResponse::fromEntity).toList());
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


@Data
class StepsInfo
{
    private Long id;
    private String stepName;

    public static StepsInfo fromEntity(TestCaseStep caseStep) {
        StepsInfo stepsInfo = new StepsInfo();
        stepsInfo.setId(caseStep.getId());
        stepsInfo.setStepName(caseStep.getStepName());
        return stepsInfo;
    }
}