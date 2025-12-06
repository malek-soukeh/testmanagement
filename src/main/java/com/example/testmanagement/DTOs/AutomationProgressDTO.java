package com.example.testmanagement.DTOs;

public class AutomationProgressDTO {
    private Long totalTests;
    private Long automatedTests;
    private Long manualTests;
    private Long performanceTests;
    private Double automationPercentage;

    public AutomationProgressDTO(Long totalTests, Long automatedTests, Long manualTests, Long performanceTests) {
        this.totalTests = totalTests;
        this.automatedTests = automatedTests;
        this.manualTests = manualTests;
        this.performanceTests = performanceTests;
        this.automationPercentage = totalTests > 0
                ? Math.round((double) (automatedTests + performanceTests) / totalTests * 100 * 10.0) / 10.0
                : 0.0;
    }

    public Long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Long totalTests) {
        this.totalTests = totalTests;
    }

    public Long getAutomatedTests() {
        return automatedTests;
    }

    public void setAutomatedTests(Long automatedTests) {
        this.automatedTests = automatedTests;
    }

    public Long getManualTests() {
        return manualTests;
    }

    public void setManualTests(Long manualTests) {
        this.manualTests = manualTests;
    }

    public Long getPerformanceTests() {
        return performanceTests;
    }

    public void setPerformanceTests(Long performanceTests) {
        this.performanceTests = performanceTests;
    }

    public Double getAutomationPercentage() {
        return automationPercentage;
    }

    public void setAutomationPercentage(Double automationPercentage) {
        this.automationPercentage = automationPercentage;
    }
}
