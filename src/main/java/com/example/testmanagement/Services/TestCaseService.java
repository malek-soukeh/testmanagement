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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseService {
    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final TestCaseStepRepository testCaseStepRepository;
    private final SeleniumExecutionService seleniumExecutionService;
    private final AuditLogService auditLogService;

    public TestCase createTestCase(CreateTestCaseRequest request, String username, Long testSuiteId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        TestSuite testSuite = testSuiteRepository.findById(testSuiteId)
                .orElseThrow(() -> new RuntimeException("Test Suite not found: " + testSuiteId));

        TestCase testCase = new TestCase();
        testCase.setTitle(request.getTitle());
        testCase.setDescription(request.getDescription());
        testCase.setPrecondition(request.getPrecondition());
        testCase.setTestType(request.getTestType() != null ? request.getTestType() : TestCase.TestType.MANUAL);
        testCase.setPriority(request.getPriority() != null ? request.getPriority() : TestCase.Priority.MEDIUM);
        testCase.setStatus(request.getStatus() != null ? request.getStatus() : TestCase.Status.DRAFT);
        testCase.setCreatedBy(user);
        testCase.setTestSuite(testSuite);
        testCase.setCreatedAt(LocalDateTime.now());

        // Pour les tests de performance : pas de steps, seulement testUrl et
        // performanceConfig
        if (request.getTestType() == TestCase.TestType.PERFORMANCE) {
            if (request.getTestUrl() == null || request.getTestUrl().isEmpty()) {
                throw new RuntimeException("testUrl is required for PERFORMANCE test cases");
            }
            testCase.setTestUrl(request.getTestUrl());

            // Sérialiser performanceConfig en JSON
            if (request.getPerformanceConfig() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String perfConfigJson = mapper.writeValueAsString(request.getPerformanceConfig());
                    testCase.setPerformanceConfig(perfConfigJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize performance config", e);
                }
            }
        } else {
            // Pour les autres types de tests, on peut avoir testUrl (pour AUTOMATED)
            if (request.getTestUrl() != null) {
                testCase.setTestUrl(request.getTestUrl());
            }
        }

        TestCase savedTestCase = testCaseRepository.save(testCase);
        auditLogService.logAction("CREATE", "TEST_CASE", savedTestCase.getId().toString(),
                "Created test case: " + savedTestCase.getTitle());

        // Pour les tests PERFORMANCE, on ne crée pas de steps
        if (request.getTestType() != TestCase.TestType.PERFORMANCE &&
                request.getSteps() != null && !request.getSteps().isEmpty()) {
            List<TestCaseStep> steps = request.getSteps().stream().map(stepReq -> {
                TestCaseStep testCaseStep = new TestCaseStep();
                testCaseStep.setStepName(stepReq.getStepName());
                testCaseStep.setExpectedResult(stepReq.getExpectedResult());
                testCaseStep.setCreatedBy(user);
                testCaseStep.setTestCase(savedTestCase);
                testCaseStep.setActionType(stepReq.getActionType());
                testCaseStep.setActionTarget(stepReq.getActionTarget());
                testCaseStep.setActionValue(stepReq.getActionValue());
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
        return testCaseRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public TestCase getTestCaseDetails(Long suiteId, Long id) {
        return testCaseRepository.findByIdAndTestSuiteIdWithSteps(id, suiteId)
                .orElseThrow(() -> new RuntimeException(
                        "Test case not found with id: " + id + " in suite: " + suiteId));
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

        // Mettre à jour les champs de base
        if (request.getTitle() != null)
            testCase.setTitle(request.getTitle());
        if (request.getDescription() != null)
            testCase.setDescription(request.getDescription());
        if (request.getPrecondition() != null)
            testCase.setPrecondition(request.getPrecondition());
        if (request.getTestType() != null)
            testCase.setTestType(request.getTestType());
        if (request.getPriority() != null)
            testCase.setPriority(request.getPriority());
        if (request.getStatus() != null)
            testCase.setStatus(request.getStatus());
        if (request.getTestUrl() != null)
            testCase.setTestUrl(request.getTestUrl());

        // Pour les tests de performance : mettre à jour performanceConfig
        if (testCase.getTestType() == TestCase.TestType.PERFORMANCE && request.getPerformanceConfig() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String perfConfigJson = mapper.writeValueAsString(request.getPerformanceConfig());
                testCase.setPerformanceConfig(perfConfigJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize performance config", e);
            }
        }

        // Mettre à jour la date de modification
        testCase.setUpdatedAt(LocalDateTime.now());

        // Sauvegarder le test case d'abord
        TestCase savedTestCase = testCaseRepository.save(testCase);
        auditLogService.logAction("UPDATE", "TEST_CASE", savedTestCase.getId().toString(),
                "Updated test case: " + savedTestCase.getTitle());

        // Pour les tests PERFORMANCE, on ne gère pas les steps
        // Gérer la mise à jour des steps si fournis (uniquement pour les autres types)
        if (testCase.getTestType() != TestCase.TestType.PERFORMANCE && request.getSteps() != null) {
            // Supprimer tous les anciens steps
            List<TestCaseStep> existingSteps = testCaseStepRepository.findByTestCaseId(id);
            if (!existingSteps.isEmpty()) {
                testCaseStepRepository.deleteAll(existingSteps);
            }

            // Créer les nouveaux steps
            if (!request.getSteps().isEmpty()) {
                List<TestCaseStep> newSteps = request.getSteps().stream().map(stepReq -> {
                    TestCaseStep testCaseStep = new TestCaseStep();
                    testCaseStep.setStepName(stepReq.getStepName());
                    testCaseStep.setExpectedResult(stepReq.getExpectedResult());
                    testCaseStep.setCreatedBy(user);
                    testCaseStep.setTestCase(savedTestCase);
                    testCaseStep.setActionType(stepReq.getActionType());
                    testCaseStep.setActionTarget(stepReq.getActionTarget());
                    testCaseStep.setActionValue(stepReq.getActionValue());
                    testCaseStep.setCreatedAt(LocalDateTime.now());
                    return testCaseStep;
                }).toList();
                testCaseStepRepository.saveAll(newSteps);
            }
        }

        return savedTestCase;
    }

    public void deleteTestCase(Long id, String username) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!testCase.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own test cases");
        }

        auditLogService.logAction("DELETE", "TEST_CASE", id.toString(), "Deleted test case: " + testCase.getTitle());
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
                "failedTestCases", testCaseRepository.countByStatus(TestCase.Status.FAILED));
    }

    public Long getUserIdByUsername(String firstName) {
        return userRepository.findByEmail(firstName)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + firstName));
    }

    public Map<String, Object> triggerAutomatedTest(Long testCaseId, Long userId,
            String jenkinsJobUrl, String jenkinsUser,
            String jenkinsToken) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase.getTestType() != TestCase.TestType.AUTOMATED &&
                testCase.getTestType() != TestCase.TestType.PERFORMANCE) {
            throw new RuntimeException("Only automated or performance tests can be triggered");
        }

        // Sérialiser le scénario en JSON
        String scenarioJson = buildSeleniumScenarioJson(testCase);

        // Appel du service Selenium / Jenkins
        Map<String, Object> response = seleniumExecutionService.triggerTestCaseViaJenkins(
                testCaseId, userId, jenkinsJobUrl, jenkinsUser, jenkinsToken, scenarioJson);

        testCase.setStatus(TestCase.Status.RUNNING);
        testCaseRepository.save(testCase);
        return response;
    }

    public Map<String, Object> executeTestSuite(Long suiteId, Long userId, String seleniumJobUrl,
            String performanceJobUrl, String jenkinsUser,
            String jenkinsToken) {
        List<TestCase> testCases = testCaseRepository.findAllByTestSuiteId(suiteId);
        List<TestCase> executableTestCases = testCases.stream()
                .filter(tc -> tc.getTestType() == TestCase.TestType.AUTOMATED
                        || tc.getTestType() == TestCase.TestType.PERFORMANCE)
                .toList();

        if (executableTestCases.isEmpty()) {
            throw new RuntimeException("No executable test cases (AUTOMATED or PERFORMANCE) found in this suite");
        }

        int triggeredCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (TestCase tc : executableTestCases) {
            try {
                String jobUrl = null;
                if (tc.getTestType() == TestCase.TestType.AUTOMATED) {
                    jobUrl = seleniumJobUrl;
                } else if (tc.getTestType() == TestCase.TestType.PERFORMANCE) {
                    jobUrl = performanceJobUrl;
                }

                if (jobUrl != null && !jobUrl.isBlank()) {
                    triggerAutomatedTest(tc.getId(), userId, jobUrl, jenkinsUser, jenkinsToken);
                    triggeredCount++;
                } else {
                    errors.add("Skipped test " + tc.getId() + ": No Jenkins URL provided for type " + tc.getTestType());
                }
            } catch (Exception e) {
                errors.add("Failed to trigger test case " + tc.getId() + ": " + e.getMessage());
                // Continue triggering others even if one fails
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("suiteId", suiteId);
        response.put("totalTestCases", testCases.size());
        response.put("executableTestCases", executableTestCases.size());
        response.put("triggeredCount", triggeredCount);
        response.put("message", "Triggered " + triggeredCount + " test cases for execution");
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return response;
    }

    public String buildSeleniumScenarioJson(TestCase testCase) {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("testCaseId", testCase.getId());
        scenario.put("title", testCase.getTitle());
        scenario.put("url", testCase.getTestUrl());
        scenario.put("testType",
                testCase.getTestType() != null ? testCase.getTestType().name() : TestCase.TestType.MANUAL.name());

        List<Map<String, Object>> steps = testCaseStepRepository.findByTestCaseIdOrderByIdAsc(testCase.getId())
                .stream()
                .map(step -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("stepName", step.getStepName());
                    map.put("actionType", step.getActionType());
                    map.put("actionTarget", step.getActionTarget());
                    map.put("actionValue", step.getActionValue());
                    map.put("expectedResult", step.getExpectedResult());
                    map.put("selectorType", inferSelectorType(step.getActionTarget()));
                    return map;
                }).collect(Collectors.toList());
        scenario.put("steps", steps);
        try {
            // Retourner un tableau avec un seul scénario pour compatibilité avec Jenkins
            List<Map<String, Object>> scenarios = List.of(scenario);
            return new ObjectMapper().writeValueAsString(scenarios);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build scenario JSON", e);
        }
    }

    private String inferSelectorType(String actionTarget) {
        if (actionTarget == null || actionTarget.isBlank()) {
            return "css";
        }
        String trimmed = actionTarget.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("./") || trimmed.startsWith("(")) {
            return "xpath";
        }
        if (trimmed.contains("@") || (trimmed.contains("[") && trimmed.contains("]"))) {
            return "xpath";
        }
        return "css";
    }

}
