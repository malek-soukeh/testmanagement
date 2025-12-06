package com.example.testmanagement.DTOs;

import java.time.LocalDateTime;

public class FailedTestDTO {
    private Long testCaseId;
    private String testCaseTitle;
    private LocalDateTime lastFailedAt;
    private Integer consecutiveFailures;
    private String errorMessage;

    public FailedTestDTO(Long testCaseId, String testCaseTitle, LocalDateTime lastFailedAt,
            Integer consecutiveFailures, String errorMessage) {
        this.testCaseId = testCaseId;
        this.testCaseTitle = testCaseTitle;
        this.lastFailedAt = lastFailedAt;
        this.consecutiveFailures = consecutiveFailures;
        this.errorMessage = errorMessage;
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getTestCaseTitle() {
        return testCaseTitle;
    }

    public void setTestCaseTitle(String testCaseTitle) {
        this.testCaseTitle = testCaseTitle;
    }

    public LocalDateTime getLastFailedAt() {
        return lastFailedAt;
    }

    public void setLastFailedAt(LocalDateTime lastFailedAt) {
        this.lastFailedAt = lastFailedAt;
    }

    public Integer getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(Integer consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
