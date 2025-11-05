package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.TestCaseStep;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestCaseStepResponse {

    private Long id;
    private String stepName;
    private String expectedResult;
    private String actualResult;
    private TestCaseStep.CaseResult result;
    private Long testCaseId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String actionType;
    private String actionTarget;
    private String actionValue;

    public static TestCaseStepResponse fromEntity(TestCaseStep step) {
        TestCaseStepResponse response = new TestCaseStepResponse();
        response.setId(step.getId());
        response.setStepName(step.getStepName());
        response.setExpectedResult(step.getExpectedResult());
        response.setActualResult(step.getActualResult());
        response.setResult(step.getResult());
        response.setTestCaseId(step.getTestCase().getId());
        response.setCreatedBy(step.getCreatedBy().getEmail());
        response.setCreatedAt(step.getCreatedAt());
        response.setUpdatedAt(step.getUpdatedAt());
        response.setActionType(step.getActionType());
        response.setActionTarget(step.getActionTarget());
        response.setActionValue(step.getActionValue());
        return response;
    }
}
