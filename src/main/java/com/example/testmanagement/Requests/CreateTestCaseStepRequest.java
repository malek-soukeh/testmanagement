package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCaseStep;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTestCaseStepRequest {

    @NotBlank(message = "Step name is required")
    private String stepName;

    private String expectedResult;
    private String actualResult;
    private TestCaseStep.CaseResult result;
    private Long testCaseId;
}
