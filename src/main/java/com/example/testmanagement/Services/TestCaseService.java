package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.CreateTestCaseRequest;
import com.example.testmanagement.Requests.UpdateTestCaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service @Transactional @RequiredArgsConstructor
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;

    public TestCase createTestCase(CreateTestCaseRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        TestCase testCase = new TestCase();
        testCase.setTitle(request.getTitle());
        testCase.setDescription(request.getDescription());
        testCase.setTestSteps(request.getTestSteps());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setTestType(request.getTestType() != null ? request.getTestType() : TestCase.TestType.MANUAL);
        testCase.setPriority(request.getPriority() != null ? request.getPriority() : TestCase.Priority.MEDIUM);
        testCase.setStatus(request.getStatus() != null ? request.getStatus() : TestCase.Status.DRAFT);
        testCase.setCreatedBy(user);

        return testCaseRepository.save(testCase);
    }

    @Transactional(readOnly = true)
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TestCase getTestCaseById(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TestCase> getTestCasesByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return testCaseRepository.findByCreatedById(user.getId());
    }

    @Transactional(readOnly = true)
    public List<TestCase> getTestCasesByType(TestCase.TestType testType) {
        return testCaseRepository.findByTestType(testType);
    }

    @Transactional(readOnly = true)
    public List<TestCase> getTestCasesByPriority(TestCase.Priority priority) {
        return testCaseRepository.findByPriority(priority);
    }

    @Transactional(readOnly = true)
    public List<TestCase> searchTestCases(String title) {
        return testCaseRepository.findByTitleContainingIgnoreCase(title);
    }

    public TestCase updateTestCase(Long id, UpdateTestCaseRequest request, String username) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!testCase.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own test cases");
        }

        if (request.getTitle() != null) testCase.setTitle(request.getTitle());
        if (request.getDescription() != null) testCase.setDescription(request.getDescription());
        if (request.getTestSteps() != null) testCase.setTestSteps(request.getTestSteps());
        if (request.getExpectedResult() != null) testCase.setExpectedResult(request.getExpectedResult());
        if (request.getTestType() != null) testCase.setTestType(request.getTestType());
        if (request.getPriority() != null) testCase.setPriority(request.getPriority());
        if (request.getStatus() != null) testCase.setStatus(request.getStatus());

        return testCaseRepository.save(testCase);
    }

    public void deleteTestCase(Long id, String username) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!testCase.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own test cases");
        }

        testCaseRepository.delete(testCase);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTestCaseStatistics() {
        return Map.of(
                "totalTestCases", testCaseRepository.count(),
                "manualTestCases", testCaseRepository.countByTestType(TestCase.TestType.MANUAL),
                "automatedTestCases", testCaseRepository.countByTestType(TestCase.TestType.AUTOMATED),
                "performanceTestCases", testCaseRepository.countByTestType(TestCase.TestType.PERFORMANCE),
                "draftTestCases", testCaseRepository.countByStatus(TestCase.Status.DRAFT),
                "readyTestCases", testCaseRepository.countByStatus(TestCase.Status.READY),
                "obsoleteTestCases", testCaseRepository.countByStatus(TestCase.Status.OBSOLETE)
        );
    }
}
