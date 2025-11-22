package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestRun;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    @Query("SELECT tr FROM TestRun tr WHERE tr.testCase.testSuite.id = :suiteId")
    List<TestRun> findByTestSuiteId(@Param("suiteId") Long suiteId);
    
    List<TestRun> findByCreatedBy(User user);
    List<TestRun> findByStatus(TestRun.RunStatus status);
}
