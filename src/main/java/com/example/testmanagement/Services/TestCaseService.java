package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestCaseStep;
import com.example.testmanagement.Entities.TestSuite;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.TestCaseStepRepository;
import com.example.testmanagement.Repository.TestSuiteRepository;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.CreateTestCaseRequest;
import com.example.testmanagement.Requests.UpdateTestCaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service @Transactional @RequiredArgsConstructor
public class TestCaseService {
    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final TestCaseStepRepository testCaseStepRepository;
    private final JenkinsService jenkinsService;
    private final SimpMessagingTemplate messagingTemplate;

    public TestCase createTestCase(CreateTestCaseRequest request, String username,Long testSuiteId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        TestSuite testSuite = testSuiteRepository.findById(testSuiteId).orElseThrow(() -> new RuntimeException("Test Suite not found: " + testSuiteId));

        TestCase testCase = new TestCase();
        testCase.setTitle(request.getTitle());
        testCase.setDescription(request.getDescription());
        testCase.setPrecondition(request.getPrecondition());
        testCase.setTestType(request.getTestType() != null ? request.getTestType() : TestCase.TestType.MANUAL);
        testCase.setPriority(request.getPriority() != null ? request.getPriority() : TestCase.Priority.MEDIUM);
        testCase.setStatus(request.getStatus() != null ? request.getStatus() : TestCase.Status.DRAFT);
        testCase.setCreatedBy(user);
        testCase.setTestSuite(testSuite);
        
        TestCase savedTestCase = testCaseRepository.save(testCase);
        
        if(request.getSteps() != null && !request.getSteps().isEmpty()) {
            List<TestCaseStep> steps = request.getSteps().stream().map(stepReq ->{
               TestCaseStep testCaseStep = new TestCaseStep();
               testCaseStep.setStepName(stepReq.getStepName());
               testCaseStep.setExpectedResult(stepReq.getExpectedResult());
               testCaseStep.setCreatedBy(user);
               testCaseStep.setTestCase(savedTestCase);
               return testCaseStep;
            }).toList();
            testCaseStepRepository.saveAll(steps);
        }

        return savedTestCase;
    }

    @Transactional(readOnly = true)
    public List<TestCase> getAllTestCases(Long suiteId) {
        return testCaseRepository.findAllByTestSuiteId(suiteId);
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
        if (request.getPrecondition() != null) testCase.setPrecondition(request.getPrecondition());
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
                "runningTestCases", testCaseRepository.countByStatus(TestCase.Status.RUNNING),
                "passedTestCases", testCaseRepository.countByStatus(TestCase.Status.PASSED),
                "failedTestCases", testCaseRepository.countByStatus(TestCase.Status.FAILED)
                );
    }
    public void triggerAutomatedTest(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case not found"));

        if (testCase.getTestType() != TestCase.TestType.AUTOMATED &&
                testCase.getTestType() != TestCase.TestType.PERFORMANCE) {
            throw new RuntimeException("Only automated or performance tests can be triggered");
        }
        jenkinsService.triggerJenkinsJob(id);
    }

    public Long getUserIdByUsername(String firstName ) {
        return userRepository.findByFirstName(firstName)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + firstName));
    }
}
