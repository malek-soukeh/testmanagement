package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
        List<TestCase> findByCreatedBy(User user);

        List<TestCase> findByCreatedById(Long userId);

        List<TestCase> findByTestType(TestCase.TestType testType);

        List<TestCase> findByPriority(TestCase.Priority priority);

        List<TestCase> findByStatus(TestCase.Status status);

        List<TestCase> findByTitleContainingIgnoreCase(String title);

        Optional<TestCase> findByIdAndCreatedById(Long id, Long userId);

        List<TestCase> findAllByTestSuiteId(Long testSuiteId);

        long countByTestType(TestCase.TestType testType);

        long countByPriority(TestCase.Priority priority);

        long countByStatus(TestCase.Status status);

        long countByTestSuite_Project_Id(Long projectId);

        List<TestCase> findAllByTestSuite_Project_Id(Long projectId);

        List<TestCase> findAllByTestSuite_Project_CreatedBy(User user);

        @Query("select tc from TestCase tc " +
                        "left join fetch tc.testSuite ts " +
                        "left join fetch ts.project " +
                        "left join fetch tc.createdBy")
        List<TestCase> findAllWithSuiteAndProject();

        @Query("select tc from TestCase tc " +
                        "left join fetch tc.testCaseSteps " +
                        "where tc.id = :id")
        Optional<TestCase> findByIdWithSteps(Long id);

        @Query("select tc from TestCase tc " +
                        "left join fetch tc.testCaseSteps " +
                        "where tc.id = :id and tc.testSuite.id = :suiteId")
        Optional<TestCase> findByIdAndTestSuiteIdWithSteps(@Param("id") Long id, @Param("suiteId") Long suiteId);
}
