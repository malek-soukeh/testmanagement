package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCase;
import lombok.Data;

@Data
public class UpdateTestCaseRequest {
    private String title;
    private String description;
    private String testSteps;
    private String expectedResult;
    private String precondition;
    private TestCase.TestType testType;
    private TestCase.Priority priority;
    private TestCase.Status status;
}