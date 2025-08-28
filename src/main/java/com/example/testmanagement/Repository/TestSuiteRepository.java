package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestSuite;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    List<TestSuite> findByProject(Project project);
    List<TestSuite> findByProjectId(Long projectId);
    List<TestSuite> findByCreatedBy(User user);
}
