package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JenkinsService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpMessagingTemplate messagingTemplate;
    private final TestCaseRepository testCaseRepository;


    private final String jenkinsUrl = "http://10.0.0.15:8080";
    private final String jobName = "selenium-runner";
    private final String username = "admin";
    private final String apiToken = "11d1741c72084f7b0ebd2144638320e8d2";
    private final ObjectMapper objectMapper;

    public void triggerJenkinsJob(Long testCaseId) {
        updateStatus(testCaseId, TestCase.Status.RUNNING);
        messagingTemplate.convertAndSend("/topic/test-status", new TestStatusNotification(testCaseId, "RUNNING"));
        new Thread(() -> {
            try {
                TestCase testCase = testCaseRepository.findById(testCaseId)
                        .orElseThrow(() -> new RuntimeException("Test case not found"));

                String scenarioJson = buildSeleniumScenarioJson(testCase);

                String encodedScenario = URLEncoder.encode(scenarioJson, StandardCharsets.UTF_8);
                String url = String.format("%s/job/%s/buildWithParameters?SCENARIO_JSON=%s",
                        jenkinsUrl, jobName, encodedScenario);

                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                Thread.sleep(8000); // Attendre le démarrage du job

                int lastBuild = getLastBuildNumber();
                boolean building;
                do {
                    Thread.sleep(10000);
                    building = isBuilding(lastBuild);
                } while (building);

                boolean success = isBuildSuccessful(lastBuild);
                TestCase.Status finalStatus = success ? TestCase.Status.PASSED : TestCase.Status.FAILED;
                updateStatus(testCaseId, finalStatus);

                messagingTemplate.convertAndSend("/topic/test-status",
                        new TestStatusNotification(testCaseId, finalStatus.toString()));
            } catch (InterruptedException | RuntimeException e) {
                updateStatus(testCaseId, TestCase.Status.FAILED);
                messagingTemplate.convertAndSend("/topic/test-status",
                        new TestStatusNotification(testCaseId, "FAILED"));
                e.printStackTrace();
            }
        }).start();
    }
    private String buildSeleniumScenarioJson(TestCase testCase) {
        try {
            Map<String, Object> scenario = new HashMap<>();
            scenario.put("testCaseId", testCase.getId());
            scenario.put("title", testCase.getTitle());
            scenario.put("url", testCase.getTestUrl());

            // Mapping des steps
            List<Map<String, Object>> steps = (testCase.getTestCaseSteps() != null)
                    ? testCase.getTestCaseSteps().stream().map(step -> {
                Map<String, Object> map = new HashMap<>();
                map.put("stepName", step.getStepName());
                map.put("actionType", step.getActionType());
                map.put("actionTarget", step.getActionTarget());
                map.put("actionValue", step.getActionValue());
                map.put("expectedResult", step.getExpectedResult());
                return map;
            }).toList()
                    : List.of();

            scenario.put("steps", steps);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build scenario JSON", e);
        }
    }

    private void updateStatus(Long id, TestCase.Status status) {
        testCaseRepository.findById(id).ifPresent(tc -> {
            tc.setStatus(status);
            testCaseRepository.save(tc);
        });
    }

    private int getLastBuildNumber() {
        String url = jenkinsUrl + "/job/" + jobName + "/api/json";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> lastBuild = (Map<String, Object>) response.getBody().get("lastBuild");
        return (int) lastBuild.get("number");
    }
    private boolean isBuilding(int buildNumber) {
        String url = String.format("%s/job/%s/%d/api/json", jenkinsUrl, jobName, buildNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return (boolean) response.getBody().get("building");
    }
    private boolean isBuildSuccessful(int buildNumber) {
        String url = String.format("%s/job/%s/%d/api/json", jenkinsUrl, jobName, buildNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return "SUCCESS".equals(response.getBody().get("result"));
    }

    public TestResultSummary getLastBuildTestResult() {
        try {
            int lastBuild = getLastBuildNumber();
            String url = String.format("%s/job/%s/%d/testReport/api/json", jenkinsUrl, jobName, lastBuild);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, apiToken);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            int total = (int) body.getOrDefault("totalCount", 0);
            int failed = (int) body.getOrDefault("failCount", 0);
            int skipped = (int) body.getOrDefault("skipCount", 0);

            boolean success = failed == 0;
            System.out.println("Total: " + total + ", Failed: " + failed + ", Skipped: " + skipped + ", Success: " + success);
            return new TestResultSummary(total, failed, skipped, success);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Object getLastBuildTestResult2() {
        try {
            int lastBuild = getLastBuildNumber();
            String url = String.format("%s/job/%s/%d/testReport/api/json", jenkinsUrl, jobName, lastBuild);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, apiToken);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) return null;


            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // DTO pour envoyer au frontend
    @Data
    @AllArgsConstructor
    public static class TestResultSummary {
        private int total;
        private int failed;
        private int skipped;
        private boolean success;
    }

    @Data
    @AllArgsConstructor
    public static class TestStatusNotification {
        private Long testCaseId;
        private String status;;
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
