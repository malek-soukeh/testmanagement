package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.TestSuite;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.ProjectRepository;
import com.example.testmanagement.Repository.TestSuiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service @Transactional
public class TestSuiteService {

    private final TestSuiteRepository testSuiteRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;


    public TestSuiteService(TestSuiteRepository testSuiteRepository, ProjectRepository projectRepository, UserService userService) {
        this.testSuiteRepository = testSuiteRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public TestSuite createTestSuite(Long projectId, TestSuite suite, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        suite.setProject(project);
        suite.setCreatedBy(user);
        suite.setCreatedAt(LocalDateTime.now());
        suite.setUpdatedAt(LocalDateTime.now());

        return testSuiteRepository.save(suite);
    }

    public List<TestSuite> getTestSuites(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        return testSuiteRepository.findByProject(project);
    }
    public TestSuite updateTestSuite(Long suiteId, TestSuite suiteDetails, String username) {
        TestSuite suite = testSuiteRepository.findById(suiteId)
                .orElseThrow(() -> new RuntimeException("Test Suite not found with id: " + suiteId));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!suite.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can update only your own test suites");
        }

        if (suiteDetails.getSuiteName() != null) {
            suite.setSuiteName(suiteDetails.getSuiteName());
        }
        if (suiteDetails.getDescription() != null) {
            suite.setDescription(suiteDetails.getDescription());
        }
        suite.setUpdatedAt(LocalDateTime.now());

        return testSuiteRepository.save(suite);
    }

    public Optional<TestSuite> getTestSuite(Long testSuiteId) {
        return testSuiteRepository.findById(testSuiteId);
    }

    public void deleteTestSuite(Long suiteId, String username) {
        TestSuite suite = testSuiteRepository.findById(suiteId)
                .orElseThrow(() -> new RuntimeException("Test Suite not found with id: " + suiteId));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!suite.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own test suites");
        }

        testSuiteRepository.delete(suite);
    }
}
