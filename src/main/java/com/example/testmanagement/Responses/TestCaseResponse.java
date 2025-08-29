package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.TestCase;
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
        return response;
    }
}