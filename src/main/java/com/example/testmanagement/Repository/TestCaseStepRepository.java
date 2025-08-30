package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestCaseStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestCaseStepRepository extends JpaRepository<TestCaseStep, Long> {
    List<TestCaseStep> findByTestCaseId(Long testCaseId);
    List<TestCaseStep> findByTestCaseIdOrderByIdAsc(Long testCaseId);
    List<TestCaseStep> findByStepName(String stepName);
    List<TestCaseStep> findByCreatedById(Long id);
    @Query("select ts from TestCaseStep ts where ts.testCase.id = :testCaseId and ts.result =:result")
    List<TestCaseStep> findByTestCaseIdAndResult(@Param("testCaseId") Long testCaseId, @Param("result") TestCaseStep.CaseResult result);
    Optional<TestCaseStep> findByIdAndTestCaseId(Long id, Long testCaseId);
    long countByTestCaseId(Long testCaseId);
    long countByTestCaseIdAndResult(Long testCaseId, TestCaseStep.CaseResult result);
}
