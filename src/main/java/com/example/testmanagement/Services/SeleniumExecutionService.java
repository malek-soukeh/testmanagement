package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.*;
import com.example.testmanagement.Repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeleniumExecutionService {

    private final TestCaseRepository testCaseRepo;
    private final TestCaseStepRepository stepRepo;
    private final TestRunRepository testRunRepo;
    private final TestResultRepository testResultRepo;
    private final UserRepository userRepo;


    public Map<String, Object> triggerTestCaseViaJenkins(Long testCaseId, Long userId,
                                                         String jenkinsJobUrl, String jenkinsUser,
                                                         String jenkinsToken) {

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

        // Sérialiser le scénario pour Jenkins (JSON)
        String scenarioJson = buildScenarioJson(testCase);

        // Déclencher le job Jenkins
        String jenkinsResponse = triggerJenkinsJob(jenkinsJobUrl, scenarioJson, jenkinsUser, jenkinsToken);

        Map<String, Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("testResultId", result.getId());
        response.put("status", run.getStatus());
        response.put("jenkinsResponse", jenkinsResponse);

        return response;
    }

    /**
     * Sérialise le test case en JSON pour Jenkins
     */
    private String buildScenarioJson(TestCase testCase) {
        // Créer une structure JSON simple, par ex. titre, steps, type
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("testCaseId", testCase.getId());
        scenario.put("title", testCase.getTitle());
        scenario.put("testType", testCase.getTestType().name());

        List<Map<String, Object>> stepsJson = new ArrayList<>();
        for (TestCaseStep step : testCase.getTestCaseSteps()) {
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("stepName", step.getStepName());
            stepMap.put("actionType", step.getActionType());
            stepMap.put("actionTarget", step.getActionTarget());
            stepMap.put("actionValue", step.getActionValue());
            stepsJson.add(stepMap);
        }
        scenario.put("steps", stepsJson);

        try {
            return new ObjectMapper().writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing test case to JSON", e);
        }
    }

    /**
     * Déclenche un job Jenkins via HTTP POST
     */
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

    /**
     * Méthode utilitaire pour récupérer le dernier résultat d'un TestRun
     */
    public TestResult getTestResult(Long testResultId) {
        return testResultRepo.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found"));
    }
}
