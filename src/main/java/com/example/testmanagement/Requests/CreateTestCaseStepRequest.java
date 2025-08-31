package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCaseStep;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTestCaseStepRequest {
    @NotBlank(message = "Step name is required")
    private String stepName;
    @NotBlank(message = "Expected Result is required")
    private String expectedResult;
    private Long testCaseId;
}
