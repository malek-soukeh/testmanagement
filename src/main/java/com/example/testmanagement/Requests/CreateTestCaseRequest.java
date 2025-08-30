package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTestCaseRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String testSteps;
    private String expectedResult;
    private String precondition;
    private TestCase.TestType testType;
    private TestCase.Priority priority;
    private TestCase.Status status;
}