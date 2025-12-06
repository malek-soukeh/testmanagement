package com.example.testmanagement.DTOs;

public class SlowestTestDTO {
    private Long testCaseId;
    private String testCaseTitle;
    private Long avgExecutionTime; // in ms
    private Long executionCount;

    public SlowestTestDTO(Long testCaseId, String testCaseTitle, Long avgExecutionTime, Long executionCount) {
        this.testCaseId = testCaseId;
        this.testCaseTitle = testCaseTitle;
        this.avgExecutionTime = avgExecutionTime;
        this.executionCount = executionCount;
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

    public Long getAvgExecutionTime() {
        return avgExecutionTime;
    }

    public void setAvgExecutionTime(Long avgExecutionTime) {
        this.avgExecutionTime = avgExecutionTime;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }
}
