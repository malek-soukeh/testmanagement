package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.*;
import com.example.testmanagement.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

        // Déclencher le job Jenkins
        String jenkinsResponse = triggerJenkinsJob(jenkinsJobUrl, scenarioJson, jenkinsUser, jenkinsToken);
        Objects.requireNonNull(scenarioJson, "scenarioJson cannot be null");
        Objects.requireNonNull(jenkinsUser, "jenkinsUser cannot be null");
        Objects.requireNonNull(jenkinsToken, "jenkinsToken cannot be null");

        return Map.of(
                "runId", run.getId(),
                "testResultId", result.getId(),
                "status", run.getStatus(),
                "jenkinsResponse", jenkinsResponse
        );
    }
    public String triggerJenkinsJob(String jenkinsJobUrl, String scenarioJson,
                                    String jenkinsUser, String jenkinsToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(jenkinsUser, jenkinsToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("SCENARIO_JSON", scenarioJson);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = rest.postForEntity(jenkinsJobUrl, request, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Jenkins job trigger failed: " + resp.getStatusCode());
        }

        return resp.getBody();
    }
    public TestResult getTestResult(Long testResultId) {
        return testResultRepo.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found"));
    }
}
