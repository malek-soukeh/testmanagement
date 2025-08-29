package com.example.testmanagement.Entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_cases")
public class TestCase {

    public enum TestType { MANUAL, AUTOMATED, PERFORMANCE }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { DRAFT, READY, OBSOLETE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String testSteps;

    @Column(columnDefinition = "TEXT")
    private String expectedResult;

    @Enumerated(EnumType.STRING)
    private TestType testType = TestType.MANUAL;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    private TestSuite testSuite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "role"})
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
