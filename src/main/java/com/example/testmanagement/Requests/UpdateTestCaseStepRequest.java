package com.example.testmanagement.Requests;

import com.example.testmanagement.Entities.TestCaseStep;
import lombok.Data;

@Data
public class UpdateTestCaseStepRequest {
    private String stepName;
    private String expectedResult;
    private String actualResult;
    private TestCaseStep.CaseResult result;
    private Long testCaseId;
}
