package com.example.testmanagement.Requests;

import com.example.testmanagement.DTOs.PerformanceConfigDTO;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestCaseStep;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

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
    private List<CreateTestCaseStepRequest> steps;
    
    // Pour les tests de performance
    private String testUrl; // URL du site à tester
    private PerformanceConfigDTO performanceConfig; // Paramètres de configuration du test de performance

}