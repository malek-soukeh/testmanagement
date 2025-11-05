package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestCaseStep;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.TestCaseStepRepository;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.CreateTestCaseStepRequest;
import com.example.testmanagement.Requests.UpdateTestCaseStepRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TestCaseStepService {
    private final TestCaseStepRepository testCaseStepRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public TestCaseStep createTestCaseStep(CreateTestCaseStepRequest request, String username) {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found!"));
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId()).orElseThrow(() -> new RuntimeException("TestCase not found!"));
        TestCaseStep step = new TestCaseStep();
        step.setStepName(request.getStepName());
        step.setExpectedResult(request.getExpectedResult());
        step.setTestCase(testCase);
        step.setCreatedBy(user);

        return testCaseStepRepository.save(step);
    }

    public TestCaseStep createBulkTestCaseStep(CreateTestCaseStepRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + request.getTestCaseId()));

        TestCaseStep step = new TestCaseStep();
        step.setStepName(request.getStepName());
        step.setExpectedResult(request.getExpectedResult());
        step.setTestCase(testCase);
        step.setCreatedBy(user);
        step.setResult(TestCaseStep.CaseResult.NATURAL);

        return testCaseStepRepository.save(step);
    }

    @Transactional(readOnly = true)
    public TestCaseStep getTestCaseStepById(Long id) {
        return testCaseStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case step not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TestCaseStep> getStepsByTestCaseId(Long testCaseId) {
        return testCaseStepRepository.findByTestCaseIdOrderByIdAsc(testCaseId);
    }

    @Transactional(readOnly = true)
    public List<TestCaseStep> getStepsByTestCaseIdAndResult(Long testCaseId, TestCaseStep.CaseResult result) {
        return testCaseStepRepository.findByTestCaseIdAndResult(testCaseId, result);
    }

    @Transactional(readOnly = true)
    public List<TestCaseStep> getStepsByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return testCaseStepRepository.findByCreatedById(user.getId());
    }

    public TestCaseStep updateTestCaseStep(Long id, UpdateTestCaseStepRequest request, String username) {
        TestCaseStep step = testCaseStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case step not found with id: " + id));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!step.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own test case steps");
        }

        if (request.getStepName() != null) step.setStepName(request.getStepName());
        if (request.getExpectedResult() != null) step.setExpectedResult(request.getExpectedResult());
        if (request.getActualResult() != null) step.setActualResult(request.getActualResult());
        if (request.getResult() != null) step.setResult(request.getResult());
        step.setUpdatedAt(LocalDateTime.now());
        return testCaseStepRepository.save(step);
    }

    public TestCaseStep updateStepResult(Long id, TestCaseStep.CaseResult result, String actualResult, String username) {
        TestCaseStep step = testCaseStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case step not found with id: " + id));

        step.setResult(result);
        if (actualResult != null) {
            step.setActualResult(actualResult);
        }
        step.setUpdatedAt(LocalDateTime.now());
        TestCase testCase = step.getTestCase();
        List<TestCaseStep> steps = testCaseStepRepository.findByTestCaseId(testCase.getId());
        boolean allPassed = steps.stream().allMatch(s -> s.getResult() == TestCaseStep.CaseResult.PASSED);
        if (allPassed) {
            testCase.setStatus(TestCase.Status.PASSED);
        } else {
            boolean anyFailed = steps.stream().anyMatch(s -> s.getResult() == TestCaseStep.CaseResult.FAILED);
            if (anyFailed) {
                testCase.setStatus(TestCase.Status.FAILED);
            } else {
                testCase.setStatus(TestCase.Status.DRAFT);
            }
        }
        testCase.setUpdatedAt(LocalDateTime.now());
        testCaseRepository.save(testCase);

        messagingTemplate.convertAndSend("/topic/test-status", Map.of(
                "testCaseId", testCase.getId(),
                "status", testCase.getStatus().name()
        ));

        return testCaseStepRepository.save(step);
    }

    public void deleteTestCaseStep(Long id, String username) {
        TestCaseStep step = testCaseStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case step not found with id: " + id));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!step.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own test case steps");
        }

        testCaseStepRepository.delete(step);
    }

    public void deleteStepsByTestCaseId(Long testCaseId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<TestCaseStep> steps = testCaseStepRepository.findByTestCaseId(testCaseId);

        boolean allOwned = steps.stream()
                .allMatch(step -> step.getCreatedBy().getId().equals(user.getId()));

        if (!allOwned) {
            throw new RuntimeException("You can only delete steps that you created");
        }

        testCaseStepRepository.deleteAll(steps);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStepStatistics(Long testCaseId) {
        return Map.of(
                "totalSteps", testCaseStepRepository.countByTestCaseId(testCaseId),
                "passedSteps", testCaseStepRepository.countByTestCaseIdAndResult(testCaseId, TestCaseStep.CaseResult.PASSED),
                "failedSteps", testCaseStepRepository.countByTestCaseIdAndResult(testCaseId, TestCaseStep.CaseResult.FAILED),
                "naturalSteps", testCaseStepRepository.countByTestCaseIdAndResult(testCaseId, TestCaseStep.CaseResult.NATURAL)
        );
    }

    public void resetStepResults(Long testCaseId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<TestCaseStep> steps = testCaseStepRepository.findByTestCaseId(testCaseId);

        steps.forEach(step -> {
            step.setResult(TestCaseStep.CaseResult.NATURAL);
            step.setActualResult(null);
        });

        testCaseStepRepository.saveAll(steps);
    }
}
