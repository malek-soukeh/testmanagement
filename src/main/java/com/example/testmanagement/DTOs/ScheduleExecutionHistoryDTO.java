package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.ScheduledTestExecution;
import java.time.LocalDateTime;

public class ScheduleExecutionHistoryDTO {
    private Long id;
    private String scheduleName;
    private String testCaseTitle;
    private LocalDateTime executionTime;
    private String status;
    private Integer executionCount;
    private Integer successCount;
    private Integer failureCount;

    public ScheduleExecutionHistoryDTO(ScheduledTestExecution schedule) {
        this.id = schedule.getId();
        this.scheduleName = schedule.getName();
        this.testCaseTitle = schedule.getTestCase().getTitle();
        this.executionTime = schedule.getLastExecutionTime();
        this.status = schedule.getLastExecutionStatus() != null ? schedule.getLastExecutionStatus().toString()
                : "PENDING";
        this.executionCount = schedule.getExecutionCount();
        this.successCount = schedule.getSuccessCount();
        this.failureCount = schedule.getFailureCount();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getTestCaseTitle() {
        return testCaseTitle;
    }

    public void setTestCaseTitle(String testCaseTitle) {
        this.testCaseTitle = testCaseTitle;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }
}
