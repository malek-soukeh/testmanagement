package com.example.testmanagement.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_results")
public class TestResult {

    public enum ResultStatus { PASSED, FAILED, BLOCKED, SKIPPED, NOT_EXECUTED }

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.NOT_EXECUTED;

    private String actualResult;
    private Integer executionTimeSeconds;
    private LocalDateTime executedAt;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "role"})
    private User executedBy;
}
