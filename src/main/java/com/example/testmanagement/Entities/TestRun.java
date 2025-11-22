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
@Table(name = "test_runs")
public class TestRun {
    public enum RunStatus { PENDING, RUNNING, PASSED, FAILED, BLOCKED }

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String runName;

    @Enumerated(EnumType.STRING)
    private RunStatus status = RunStatus.PENDING;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "testResults"})
    private TestCase testCase;
    
    private String jenkinsQueueUrl;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "role"})
    private User createdBy;

    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL)
    private List<TestResult> testResults = new ArrayList<>();
}
