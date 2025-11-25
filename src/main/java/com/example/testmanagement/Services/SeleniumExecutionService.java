package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.TestRun;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.TestResultRepository;
import com.example.testmanagement.Repository.TestRunRepository;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.SeleniumCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeleniumExecutionService {

    private final TestCaseRepository testCaseRepo;
    private final TestRunRepository testRunRepo;
    private final TestResultRepository testResultRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;


    public Map<String, Object> triggerTestCaseViaJenkins(Long testCaseId, Long userId,
                                                         String jenkinsJobUrl, String jenkinsUser,
                                                         String jenkinsToken, String scenarioJson) {

        TestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("TestCase not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Créer un TestRun dans la DB
        TestRun run = new TestRun();
        run.setTestCase(testCase);
        run.setRunName("Jenkins Run - " + testCase.getTitle() + " - " + LocalDateTime.now());
        run.setStatus(TestRun.RunStatus.RUNNING);
        run.setCreatedBy(user);
        run.setCreatedAt(LocalDateTime.now());
        run.setStartedAt(LocalDateTime.now());
        testRunRepo.save(run);

        // Créer un TestResult initial
        TestResult result = new TestResult();
        result.setTestCase(testCase);
        result.setTestRun(run);
        result.setExecutedBy(user);
        result.setExecutedAt(LocalDateTime.now());
        result.setTestName(testCase.getTitle());
        result.setTestType(testCase.getTestType().name());
        result.setStatus(TestResult.ResultStatus.RUNNING);
        testResultRepo.save(result);

        run.getTestResults().add(result);
        testRunRepo.save(run);

        // Vérifier les paramètres avant d'appeler Jenkins
        Objects.requireNonNull(scenarioJson, "scenarioJson cannot be null");
        Objects.requireNonNull(jenkinsUser, "jenkinsUser cannot be null");
        Objects.requireNonNull(jenkinsToken, "jenkinsToken cannot be null");
        Objects.requireNonNull(jenkinsJobUrl, "jenkinsJobUrl cannot be null");

        // Déclencher le job Jenkins
        String jenkinsResponse = triggerJenkinsJob(jenkinsJobUrl, scenarioJson, jenkinsUser, jenkinsToken, result.getId());
        
        // Utiliser HashMap pour permettre les valeurs null
        Map<String, Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("testResultId", result.getId());
        response.put("status", run.getStatus() != null ? run.getStatus().name() : "UNKNOWN");
        response.put("jenkinsResponse", jenkinsResponse != null ? jenkinsResponse : "Jenkins job triggered successfully");
        
        return response;
    }
    public String triggerJenkinsJob(String jenkinsJobUrl, String scenarioJson,
                                    String jenkinsUser, String jenkinsToken, Long testResultId) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(jenkinsUser, jenkinsToken);

        // Encoder le JSON pour l'URL (buildWithParameters attend les paramètres en query string)
        try {
            String encodedJson = java.net.URLEncoder.encode(scenarioJson, java.nio.charset.StandardCharsets.UTF_8);
            String encodedResultId = java.net.URLEncoder.encode(String.valueOf(testResultId), java.nio.charset.StandardCharsets.UTF_8);
            String urlWithParams = jenkinsJobUrl + "?SCENARIO_JSON=" + encodedJson + "&TEST_RESULT_ID=" + encodedResultId;
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rest.postForEntity(urlWithParams, request, String.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Jenkins job trigger failed: " + resp.getStatusCode());
            }

            return resp.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger Jenkins job", e);
        }
    }
    public TestResult getTestResult(Long testResultId) {
        return testResultRepo.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found"));
    }

    public void handleSeleniumCallback(Long testResultId, SeleniumCallbackRequest payload) {
        TestResult result = testResultRepo.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found for callback"));

        boolean passed = payload != null && "PASSED".equalsIgnoreCase(payload.getStatus());
        result.setStatus(passed ? TestResult.ResultStatus.PASSED : TestResult.ResultStatus.FAILED);
        result.setExecutionReport(payload != null ? payload.getSummaryJson() : null);
        result.setArtifactUrl(payload != null ? payload.getArtifactUrl() : null);
        result.setExecutedAt(LocalDateTime.now());
        testResultRepo.save(result);

        TestRun run = result.getTestRun();
        if (run != null) {
            run.setCompletedAt(LocalDateTime.now());
            run.setStatus(passed ? TestRun.RunStatus.PASSED : TestRun.RunStatus.FAILED);
            testRunRepo.save(run);
        }

        TestCase testCase = result.getTestCase();
        if (testCase != null) {
            updateTestCaseStatus(testCase, passed ? TestCase.Status.PASSED : TestCase.Status.FAILED);
        }
    }

    private void updateTestCaseStatus(TestCase testCase, TestCase.Status status) {
        testCase.setStatus(status);
        testCase.setUpdatedAt(LocalDateTime.now());
        testCaseRepo.save(testCase);

        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseId", testCase.getId());
        payload.put("status", status.name());
        messagingTemplate.convertAndSend("/topic/test-status", payload);
    }
}
