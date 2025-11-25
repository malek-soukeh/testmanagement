package com.example.testmanagement.DTOs;

import lombok.Data;

@Data
public class SeleniumStepSummary {
    private Integer stepIndex;
    private String stepName;
    private Boolean success;
    private String screenshot;
    private String screenshotBase64;
    private String expectedResult;
    private String actualResult;
}


