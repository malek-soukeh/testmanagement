package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCase;
import lombok.Data;

import java.util.List;

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
    private List<CreateTestCaseStepRequest> steps; // Liste des steps à mettre à jour
    private String testUrl; // URL pour les tests automatisés
}