package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.ScheduledTestExecution;
import java.time.LocalDateTime;

public class UpcomingScheduleDTO {
    private Long id;
    private String name;
    private String testCaseTitle;
    private String scheduleType;
    private LocalDateTime nextExecutionTime;
    private String status;

    public UpcomingScheduleDTO(ScheduledTestExecution schedule) {
        this.id = schedule.getId();
        this.name = schedule.getName();
        this.testCaseTitle = schedule.getTestCase().getTitle();
        this.scheduleType = schedule.getScheduleType().toString();
        this.nextExecutionTime = schedule.getNextExecutionTime();
        this.status = schedule.getLastExecutionStatus() != null ? schedule.getLastExecutionStatus().toString()
                : "PENDING";
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

    public String getTestCaseTitle() {
        return testCaseTitle;
    }

    public void setTestCaseTitle(String testCaseTitle) {
        this.testCaseTitle = testCaseTitle;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
