package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Repository.TestCaseRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JenkinsService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpMessagingTemplate messagingTemplate;
    private final TestCaseRepository testCaseRepository;


    private final String jenkinsUrl = "http://10.0.0.15:8080";
    private final String jobName = "test";
    private final String username = "admin";
    private final String apiToken = "11d1741c72084f7b0ebd2144638320e8d2";

    public void triggerJenkinsJob(Long testCaseId) {
        updateStatus(testCaseId, TestCase.Status.RUNNING);
        messagingTemplate.convertAndSend("/topic/test-status", new TestStatusNotification(testCaseId, "RUNNING"));
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                String url = String.format("%s/job/%s/build", jenkinsUrl, jobName);
                headers.setBasicAuth(username, apiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                restTemplate.exchange(url, HttpMethod.POST,entity, String.class);
                Thread.sleep(8000); // attendre le démarrage du job
                int lastBuild = getLastBuildNumber();
                boolean building;
                do {
                    Thread.sleep(10000);
                    building = isBuilding(lastBuild);
                } while (building);
                boolean success = isBuildSuccessful(lastBuild);
                TestCase.Status finalStatus = success ? TestCase.Status.PASSED : TestCase.Status.FAILED;
                updateStatus(testCaseId, finalStatus);
                messagingTemplate.convertAndSend("/topic/test-status", new TestStatusNotification(testCaseId, finalStatus.toString()));
            } catch (InterruptedException e) {
                updateStatus(testCaseId, TestCase.Status.FAILED);
                messagingTemplate.convertAndSend("/topic/test-status", new TestStatusNotification(testCaseId, "FAILED"));
                e.printStackTrace();
            }   }).start();}

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
}
