package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase,Long> {
    List<TestCase> findByCreatedBy(User user);
    List<TestCase> findByTestType(TestCase.TestType testType);
    List<TestCase> findByPriority(TestCase.Priority priority);
}
