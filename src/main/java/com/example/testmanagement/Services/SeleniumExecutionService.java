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
        String jenkinsResponse = triggerJenkinsJob(jenkinsJobUrl, scenarioJson, jenkinsUser, jenkinsToken,
                result.getId());

        // Utiliser HashMap pour permettre les valeurs null
        Map<String, Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("testResultId", result.getId());
        response.put("status", run.getStatus() != null ? run.getStatus().name() : "UNKNOWN");
        response.put("jenkinsResponse",
                jenkinsResponse != null ? jenkinsResponse : "Jenkins job triggered successfully");

        return response;
    }

    public String triggerJenkinsJob(String jenkinsJobUrl, String scenarioJson,
            String jenkinsUser, String jenkinsToken, Long testResultId) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(jenkinsUser, jenkinsToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        // Liste pour stocker les cookies reçus lors de la récupération du crumb
        List<String> cookies = new ArrayList<>();

        // Tenter de récupérer le Crumb CSRF
        try {
            // Extraire l'URL de base (avant /job/)
            String baseUrl = jenkinsJobUrl;
            if (jenkinsJobUrl.contains("/job/")) {
                baseUrl = jenkinsJobUrl.split("/job/")[0];
            } else {
                // Fallback si l'URL ne contient pas /job/ (peu probable pour une URL de job)
                java.net.URL url = new java.net.URL(jenkinsJobUrl);
                baseUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
            }

            String crumbUrl = baseUrl + "/crumbIssuer/api/json";

            HttpEntity<String> crumbRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> crumbResponse = rest.exchange(crumbUrl, org.springframework.http.HttpMethod.GET,
                    crumbRequest, Map.class);

            if (crumbResponse.getStatusCode().is2xxSuccessful()) {
                // Récupérer les cookies de la réponse (ex: JSESSIONID)
                if (crumbResponse.getHeaders().containsKey(HttpHeaders.SET_COOKIE)) {
                    cookies.addAll(crumbResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
                }

                if (crumbResponse.getBody() != null) {
                    Map<String, String> crumbData = (Map<String, String>) crumbResponse.getBody();
                    String crumb = crumbData.get("crumb");
                    String crumbField = crumbData.get("crumbRequestField");
                    if (crumb != null && crumbField != null) {
                        headers.set(crumbField, crumb);
                    }
                }
            }
        } catch (Exception e) {
            // Ignorer les erreurs de récupération du crumb (peut-être que CSRF est
            // désactivé ou l'URL est différente)
            System.out.println("Could not fetch Jenkins CSRF crumb: " + e.getMessage());
        }

        // Ajouter les cookies à la requête suivante si présents
        if (!cookies.isEmpty()) {
            headers.put(HttpHeaders.COOKIE, cookies);
        }

        try {
            // Préparer les paramètres en tant que Form Data
            org.springframework.util.MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
            map.add("SCENARIO_JSON", scenarioJson);
            map.add("TEST_RESULT_ID", String.valueOf(testResultId));

            // S'assurer que l'URL se termine par /buildWithParameters
            String triggerUrl = jenkinsJobUrl;
            if (!triggerUrl.endsWith("/")) {
                triggerUrl += "/";
            }
            if (!triggerUrl.endsWith("buildWithParameters")) {
                triggerUrl += "buildWithParameters";
            }

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> resp = rest.postForEntity(triggerUrl, request, String.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Jenkins job trigger failed: " + resp.getStatusCode());
            }

            return resp.getBody();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to trigger Jenkins job. URL: " + jenkinsJobUrl + ". Error: " + e.getMessage(), e);
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
