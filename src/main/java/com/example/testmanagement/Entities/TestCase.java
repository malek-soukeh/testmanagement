package com.example.testmanagement.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_cases")
public class TestCase {

    public enum TestType { MANUAL, AUTOMATED, PERFORMANCE }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { PASSED,DRAFT, READY, RUNNING , FAILED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Column(columnDefinition = "TEXT")
    private String precondition;

    @Enumerated(EnumType.STRING)
    private TestType testType = TestType.MANUAL;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    private TestSuite testSuite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "role"})
    private User createdBy;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    @JsonIgnoreProperties({"testCaseSteps"})
    private List<TestCaseStep> testCaseSteps;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String testUrl ;

    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> testResults = new ArrayList<>();
}
