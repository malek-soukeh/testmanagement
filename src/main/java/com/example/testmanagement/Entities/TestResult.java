package com.example.testmanagement.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_results")
public class TestResult {

    public enum ResultStatus { RUNNING, PASSED, FAILED , PENDING}

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    @JsonIgnoreProperties({"testCase", "testResults", "createdBy", "hibernateLazyInitializer", "handler"})
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    @JsonIgnoreProperties({"testResults", "testCaseSteps", "testSuite", "createdBy", "hibernateLazyInitializer", "handler"})
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.FAILED;

    private String actualResult;
    private Integer executionTimeSeconds;
    private LocalDateTime executedAt;
    private String notes;
    private String testName;
    private String testType;
    @Column(columnDefinition = "LONGTEXT")
    private String executionReport;
    @Column(columnDefinition = "TEXT")
    private String artifactUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User executedBy;

    private Double avgResponseTimeMs;
    private Double maxResponseTimeMs;
    private Double p95ResponseTimeMs;
    private Double errorRatePercent;
    private String jmeterReportUrl;


}
