package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Requests.TestRequest;
import com.example.testmanagement.Responses.TestResultDTO;
import com.example.testmanagement.Services.GenerationPdf;
import com.example.testmanagement.Services.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/performance-tests")
@RequiredArgsConstructor
public class PerformanceTestController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpMessagingTemplate messagingTemplate;
    private final GenerationPdf generationPdf;

    private final TestResultService testResultService;
    String jenkinsUrl = "http://10.0.0.15:8080/job/JmeterTest/buildWithParameters";
    private final String username = "admin";
    private final String apiToken = "11d1741c72084f7b0ebd2144638320e8d2";
    @PostMapping("/run")
    public ResponseEntity<String> runTest(@RequestBody TestRequest request) {
        triggerJenkinsJob(request);
        return ResponseEntity.ok("Test déclenché avec succès pour le type : " + request.getTestType());
    }
    public String  triggerJenkinsJob(TestRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, apiToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("testType", request.getTestType());
        body.add("threads", String.valueOf(request.getThreads()));
        body.add("rampup", String.valueOf(request.getRampup()));
        body.add("loops", String.valueOf(request.getLoops()));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(jenkinsUrl, entity, String.class);
        return jenkinsUrl;

    }


    @GetMapping("/last-result")
    public ResponseEntity<?> getLastBuildResult() {
        String jenkinsBaseUrl = "http://10.0.0.15:8080";
        String jobName = "JmeterTest";
        try {
            String apiUrl = jenkinsBaseUrl + "/job/"+ jobName+"/lastBuild/api/json";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, apiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du résultat : " + e.getMessage());
        }
    }

    @PostMapping("/jenkins-callback")
    public ResponseEntity<String> handleJenkinsCallback(@RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String testType = payload.getOrDefault("testType", "PERFORMANCE"); // <- ici
        TestResultDTO dto = new TestResultDTO();
        dto.setTestName("JenkinsPerformanceTest");
        dto.setTestType(testType);
        dto.setStatus("PASSED".equalsIgnoreCase(status)
                ? TestResult.ResultStatus.PASSED
                : TestResult.ResultStatus.FAILED);
        dto.setExecutedAt(LocalDateTime.now());
        dto.setActualResult("Résultat reçu depuis Jenkins : " + status);
        testResultService.saveFromDto(dto);
        messagingTemplate.convertAndSend("/topic/test-results", dto);

        return ResponseEntity.ok("Résultat Jenkins traité : " + status);
    }


    /**
     * Récupérer les logs du dernier build
     */
    @GetMapping("/last-log")
    public ResponseEntity<String> getLastBuildLog() {
        String jenkinsBaseUrl = "http://10.0.0.15:8080";
        String jobName = "JmeterTest";
        try {
            String logUrl = jenkinsBaseUrl + "/job/" + jobName + "/lastBuild/consoleText";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, apiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    logUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des logs : " + e.getMessage());
        }
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generatePdfReport() {
        try {
            String jenkinsBaseUrl = "http://10.0.0.15:8080";
            String jobName = "JmeterTest";
            String xmlUrl = jenkinsBaseUrl + "/job/" + jobName + "/lastBuild/artifact/standardResults.xml";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, apiToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> xmlResponse = restTemplate.exchange(
                    xmlUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!xmlResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(("Erreur récupération XML Jenkins").getBytes());
            }

            String xmlContent = xmlResponse.getBody();
            byte[] pdfBytes = generationPdf.generatePdfFromXml(xmlContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur génération PDF : " + e.getMessage()).getBytes());
        }
    }







}
