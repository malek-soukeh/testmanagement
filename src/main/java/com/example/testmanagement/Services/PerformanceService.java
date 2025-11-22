package com.example.testmanagement.Services;

import com.example.testmanagement.config.JenkinsConfig;
import com.example.testmanagement.DTOs.PerformanceConfigDTO;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Entities.TestRun;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.TestResultRepository;
import com.example.testmanagement.Repository.TestRunRepository;
import com.example.testmanagement.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceService {

    private final TestCaseRepository testCaseRepo;
    private final UserRepository userRepo;
    private final TestRunRepository testRunRepo;
    private final TestResultRepository testResultRepo;
    private final JenkinsConfig jenkinsConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Déclenche un test de performance via Jenkins
     * Utilise la configuration Jenkins depuis application.properties
     */
    public Map<String,Object> triggerPerformanceTest(Long testCaseId, Long userId) {
        return triggerPerformanceTest(testCaseId, userId, 
            jenkinsConfig.getJobUrl(), 
            jenkinsConfig.getUser(), 
            jenkinsConfig.getToken());
    }

    /**
     * Déclenche un test de performance via Jenkins avec des credentials personnalisés
     */
    public Map<String,Object> triggerPerformanceTest(Long testCaseId, Long userId,
                                                     String jenkinsJobUrl, String jenkinsUser,
                                                     String jenkinsToken) {

        TestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("TestCase not found"));

        // Vérifier que c'est bien un test de performance
        if (testCase.getTestType() != TestCase.TestType.PERFORMANCE) {
            throw new RuntimeException("TestCase must be of type PERFORMANCE");
        }

        // Vérifier que testUrl est présent
        if (testCase.getTestUrl() == null || testCase.getTestUrl().isEmpty()) {
            throw new RuntimeException("testUrl is required for PERFORMANCE test cases");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Créer TestRun
        TestRun run = new TestRun();
        run.setRunName("Perf Run - " + testCase.getTitle() + " - " + LocalDateTime.now());
        run.setStatus(TestRun.RunStatus.RUNNING);
        run.setStartedAt(LocalDateTime.now());
        run.setTestCase(testCase);
        run.setCreatedBy(user);
        run.setCreatedAt(LocalDateTime.now());
        testRunRepo.save(run);

        // Créer TestResult
        TestResult result = new TestResult();
        result.setTestName(testCase.getTitle());
        result.setTestType(TestCase.TestType.PERFORMANCE.name());
        result.setStatus(TestResult.ResultStatus.RUNNING);
        result.setTestRun(run);
        result.setExecutedBy(user);
        result.setExecutedAt(LocalDateTime.now());
        testResultRepo.save(result);

        run.getTestResults().add(result);
        testRunRepo.save(run);

        // Construire le scénario JSON pour Jenkins
        Map<String,Object> scenario = buildPerformanceScenario(testCase);

        // Préparer l'appel Jenkins
        String scenarioJson;
        try {
            scenarioJson = objectMapper.writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize scenario JSON", e);
        }

        // call jenkins - use form data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(jenkinsUser, jenkinsToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("SCENARIO_JSON", scenarioJson);
        body.add("TEST_RESULT_ID", result.getId().toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(jenkinsJobUrl, request, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() && resp.getStatusCode() != HttpStatus.CREATED) {
            // mark result failed
            result.setStatus(TestResult.ResultStatus.FAILED);
            testResultRepo.save(result);
            run.setStatus(TestRun.RunStatus.FAILED);
            testRunRepo.save(run);
            throw new RuntimeException("Jenkins job trigger failed: " + resp.getStatusCode());
        }

        // set queue url if available (Location header)
        if (resp.getHeaders().containsKey(HttpHeaders.LOCATION)) {
            run.setJenkinsQueueUrl(resp.getHeaders().getLocation().toString());
            testRunRepo.save(run);
        }

        Map<String,Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("testResultId", result.getId());
        response.put("status", run.getStatus());
        response.put("jenkinsResponse", resp.getBody());
        return response;
    }

    public void handleJenkinsCallback(Long testResultId, Map<String,Object> metrics) {
        TestResult result = testResultRepo.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found"));

        // parse metrics safely
        Double avg = toDouble(metrics.get("avgResponseTimeMs"));
        Double max = toDouble(metrics.get("maxResponseTimeMs"));
        Double p95 = toDouble(metrics.get("p95ResponseTimeMs"));
        Double errRate = toDouble(metrics.get("errorRatePercent"));
        String jmeterReportUrl = (String) metrics.getOrDefault("jmeterReportUrl", null);
        String status = (String) metrics.getOrDefault("status", "PASSED");

        result.setAvgResponseTimeMs(avg);
        result.setMaxResponseTimeMs(max);
        result.setP95ResponseTimeMs(p95);
        result.setErrorRatePercent(errRate);
        result.setJmeterReportUrl(jmeterReportUrl);
        result.setStatus("PASSED".equalsIgnoreCase(status) ? TestResult.ResultStatus.PASSED : TestResult.ResultStatus.FAILED);
        testResultRepo.save(result);

        // update run
        TestRun run = result.getTestRun();
        if (run != null) {
            run.setCompletedAt(LocalDateTime.now());
            run.setStatus(result.getStatus() == TestResult.ResultStatus.PASSED ? TestRun.RunStatus.PASSED : TestRun.RunStatus.FAILED);
            testRunRepo.save(run);
        }

        // optionally send WebSocket/SSE event to frontend
    }

    public TestResult getTestResult(Long id) {
        return testResultRepo.findById(id).orElseThrow(() -> new RuntimeException("TestResult not found"));
    }

    /**
     * Construit le scénario JSON pour Jenkins à partir d'un TestCase
     */
    private Map<String, Object> buildPerformanceScenario(TestCase testCase) {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("testCaseId", testCase.getId());
        scenario.put("title", testCase.getTitle());
        scenario.put("url", testCase.getTestUrl());
        
        // Parser performanceConfig depuis JSON
        Map<String, Object> perfConfig = new HashMap<>();
        if (testCase.getPerformanceConfig() != null && !testCase.getPerformanceConfig().isEmpty()) {
            try {
                PerformanceConfigDTO configDTO = objectMapper.readValue(
                    testCase.getPerformanceConfig(), 
                    PerformanceConfigDTO.class
                );
                
                // Convertir le DTO en Map pour le scénario
                perfConfig.put("testType", configDTO.getTestType());
                perfConfig.put("numberOfUsers", configDTO.getNumberOfUsers());
                perfConfig.put("durationSeconds", configDTO.getDurationSeconds());
                perfConfig.put("rampUpSeconds", configDTO.getRampUpSeconds());
                perfConfig.put("requestsPerSecond", configDTO.getRequestsPerSecond());
                perfConfig.put("timeoutMs", configDTO.getTimeoutMs());
                
                // Ajouter les paramètres supplémentaires
                if (configDTO.getAdditionalParams() != null) {
                    perfConfig.putAll(configDTO.getAdditionalParams());
                }
            } catch (Exception e) {
                // Si le parsing échoue, essayer de parser directement en Map
                try {
                    perfConfig = objectMapper.readValue(
                        testCase.getPerformanceConfig(), 
                        new TypeReference<Map<String, Object>>(){}
                    );
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to parse performance config", ex);
                }
            }
        }
        
        scenario.put("performance", perfConfig);
        return scenario;
    }

    private Double toDouble(Object o) {
        if (o == null) return null;
        try {
            return Double.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}
