package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.TestResult;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Getter
@Setter
public class TestResultDTO {
    private TestResult.ResultStatus status;
    private String actualResult;
    private Integer executionTimeSeconds;
    private LocalDateTime executedAt;
    private String notes;
    private String testName;
    private String testType;

}
