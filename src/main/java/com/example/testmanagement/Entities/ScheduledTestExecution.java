package com.example.testmanagement.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_test_executions")
public class ScheduledTestExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType scheduleType;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    // private User assignedTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_execution_status")
    private ExecutionStatus lastExecutionStatus;

    @Column(name = "execution_count")
    private Integer executionCount = 0;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    public enum ScheduleType {
        ONE_TIME,
        DAILY,
        WEEKLY,
        MONTHLY
    }

    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        PASSED,
        FAILED
    }

    // Constructors
    public ScheduledTestExecution() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ExecutionStatus getLastExecutionStatus() {
        return lastExecutionStatus;
    }

    public void setLastExecutionStatus(ExecutionStatus lastExecutionStatus) {
        this.lastExecutionStatus = lastExecutionStatus;
    }

    public Integer getExecutionCount() {
        return executionCount != null ? executionCount : 0;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public Integer getSuccessCount() {
        return successCount != null ? successCount : 0;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount != null ? failureCount : 0;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    // assignedTo removed as per request

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
