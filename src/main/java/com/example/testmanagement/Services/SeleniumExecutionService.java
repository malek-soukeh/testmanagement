package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.*;
import com.example.testmanagement.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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


    public Map<String, Object> triggerTestCaseViaJenkins(Long testCaseId, Long userId,
                                                         String jenkinsJobUrl, String jenkinsUser,
                                                         String jenkinsToken, String scenarioJson) {

        TestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("TestCase not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Créer un TestRun dans la DB
        TestRun run = new TestRun();
        run.setRunName("Jenkins Run - " + testCase.getTitle() + " - " + LocalDateTime.now());
        run.setTestSuite(testCase.getTestSuite());
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
        String jenkinsResponse = triggerJenkinsJob(jenkinsJobUrl, scenarioJson, jenkinsUser, jenkinsToken);
        
        // Utiliser HashMap pour permettre les valeurs null
        Map<String, Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("testResultId", result.getId());
        response.put("status", run.getStatus() != null ? run.getStatus().name() : "UNKNOWN");
        response.put("jenkinsResponse", jenkinsResponse != null ? jenkinsResponse : "Jenkins job triggered successfully");
        
        return response;
    }
    public String triggerJenkinsJob(String jenkinsJobUrl, String scenarioJson,
                                    String jenkinsUser, String jenkinsToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(jenkinsUser, jenkinsToken);

        // Encoder le JSON pour l'URL (buildWithParameters attend les paramètres en query string)
        try {
            String encodedJson = java.net.URLEncoder.encode(scenarioJson, java.nio.charset.StandardCharsets.UTF_8);
            String urlWithParams = jenkinsJobUrl + "?SCENARIO_JSON=" + encodedJson;
            
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
}
