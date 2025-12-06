package com.example.testmanagement.DTOs;

public class SchedulerStatsDTO {
    private Integer totalSchedules;
    private Integer activeSchedules;
    private Integer executionsToday;
    private Integer executionsThisWeek;
    private Double successRate;
    private String nextExecution;

    public SchedulerStatsDTO() {
    }

    // Getters and Setters
    public Integer getTotalSchedules() {
        return totalSchedules;
    }

    public void setTotalSchedules(Integer totalSchedules) {
        this.totalSchedules = totalSchedules;
    }

    public Integer getActiveSchedules() {
        return activeSchedules;
    }

    public void setActiveSchedules(Integer activeSchedules) {
        this.activeSchedules = activeSchedules;
    }

    public Integer getExecutionsToday() {
        return executionsToday;
    }

    public void setExecutionsToday(Integer executionsToday) {
        this.executionsToday = executionsToday;
    }

    public Integer getExecutionsThisWeek() {
        return executionsThisWeek;
    }

    public void setExecutionsThisWeek(Integer executionsThisWeek) {
        this.executionsThisWeek = executionsThisWeek;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public String getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(String nextExecution) {
        this.nextExecution = nextExecution;
    }
}
