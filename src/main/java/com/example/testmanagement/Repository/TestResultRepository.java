package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult,Long> {
    List<TestResult> findByTestRunId(Long runId);
    List<TestResult> findByExecutedBy(User user);
    long countByTestRunIdAndStatus(Long runId, TestResult.ResultStatus status);
}
