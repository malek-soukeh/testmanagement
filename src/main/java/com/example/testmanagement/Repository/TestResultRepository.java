package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult,Long> {
    List<TestResult> findByTestRunId(Long runId);
    List<TestResult> findByExecutedBy(User user);
    long countByTestRunIdAndStatus(Long runId, TestResult.ResultStatus status);

    @Query("SELECT tr FROM TestResult tr " +
            "JOIN tr.testCase tc " +
            "JOIN tc.testSuite ts " +
            "JOIN ts.project p " +
            "WHERE p.id = :projectId")
    List<TestResult> findByProjectId(@Param("projectId") Long projectId);
}
