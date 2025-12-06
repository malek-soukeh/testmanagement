package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.ScheduledTestExecution;
import java.time.LocalDateTime;

public class ScheduleRequest {
    private String name;
    private Long testCaseId;
    private ScheduledTestExecution.ScheduleType scheduleType;
    private LocalDateTime executionTime;
    private Integer hour;
    private Integer minute;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Boolean active;

    // Getters and Setters
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

    public ScheduledTestExecution.ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduledTestExecution.ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    private Long assignedToUserId;

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
