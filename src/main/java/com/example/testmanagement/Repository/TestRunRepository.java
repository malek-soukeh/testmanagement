package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestRun;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    List<TestRun> findByTestSuiteId(Long suiteId);
    List<TestRun> findByCreatedBy(User user);
    List<TestRun> findByStatus(TestRun.RunStatus status);
}
