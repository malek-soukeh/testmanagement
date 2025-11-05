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
@Table(name = "projects")
public class Project {

    public enum Status { ACTIVE, MAINTENANCE, ARCHIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "role"})
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("project")
    private List<TestSuite> testSuites = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private Long teamSize;
    private LocalDateTime lastActivity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    public Long getTotalTestCases() {
        if (testSuites == null || testSuites.isEmpty()) {
            return 0L;
        }
        return testSuites.stream()
                .mapToLong(testSuite ->
                        testSuite.getTestCases() != null ? testSuite.getTestCases().size() : 0L
                )
                .sum();
    }

    @Transient
    public Long getTotalTestSuites() {
        if (testSuites == null || testSuites.isEmpty()) {
            return 0L;
        }
        return (long) testSuites.size();
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }
}
