package com.example.testmanagement.DTOs;

public class MostFailingTestDTO {
    private Long testCaseId;
    private String testCaseTitle;
    private Long failureCount;

    public MostFailingTestDTO(Long testCaseId, String testCaseTitle, Long failureCount) {
        this.testCaseId = testCaseId;
        this.testCaseTitle = testCaseTitle;
        this.failureCount = failureCount;
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

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }
}
