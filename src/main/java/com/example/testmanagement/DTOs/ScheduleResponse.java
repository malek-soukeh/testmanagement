package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.ScheduledTestExecution;
import java.time.LocalDateTime;

public class ScheduleResponse {
    private Long id;
    private String name;
    private Long testCaseId;
    private String testCaseTitle;
    private ScheduledTestExecution.ScheduleType scheduleType;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private Boolean active;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private Long assignedToUserId;
    private String assignedToUsername;

    public ScheduleResponse(ScheduledTestExecution schedule) {
        this.id = schedule.getId();
        this.name = schedule.getName();
        this.testCaseId = schedule.getTestCase().getId();
        this.testCaseTitle = schedule.getTestCase().getTitle();
        this.scheduleType = schedule.getScheduleType();
        this.nextExecutionTime = schedule.getNextExecutionTime();
        this.lastExecutionTime = schedule.getLastExecutionTime();
        this.active = schedule.getActive();
        this.createdByUsername = schedule.getCreatedBy() != null ? schedule.getCreatedBy().getEmail() : null;
        this.createdAt = schedule.getCreatedAt();
        this.assignedToUserId = schedule.getAssignedTo() != null ? schedule.getAssignedTo().getId() : null;
        this.assignedToUsername = schedule.getAssignedTo() != null
                ? (schedule.getAssignedTo().getFirstName() + " " + schedule.getAssignedTo().getLastName())
                : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ScheduledTestExecution.ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduledTestExecution.ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public LocalDateTime getLastExecutionTime() {
        return lastExecutionTime;
    }

    public void setLastExecutionTime(LocalDateTime lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
}
