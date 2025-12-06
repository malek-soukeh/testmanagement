package com.example.testmanagement.DTOs;

public class DashboardStatsDTO {
    private long totalProjects;
    private long totalTestCases;
    private double overallPassRate;
    private long criticalIssues; // Failed tests today or similar proxy

    public DashboardStatsDTO(long totalProjects, long totalTestCases, double overallPassRate, long criticalIssues) {
        this.totalProjects = totalProjects;
        this.totalTestCases = totalTestCases;
        this.overallPassRate = overallPassRate;
        this.criticalIssues = criticalIssues;
    }

    // Getters and Setters
    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(long totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public double getOverallPassRate() {
        return overallPassRate;
    }

    public void setOverallPassRate(double overallPassRate) {
        this.overallPassRate = overallPassRate;
    }

    public long getCriticalIssues() {
        return criticalIssues;
    }

    public void setCriticalIssues(long criticalIssues) {
        this.criticalIssues = criticalIssues;
    }
}
