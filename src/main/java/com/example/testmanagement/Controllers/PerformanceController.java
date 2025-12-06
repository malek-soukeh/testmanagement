package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Repository.TestResultRepository;
import com.example.testmanagement.Services.PerformanceService;
import com.example.testmanagement.Services.TestCaseService;
import com.example.testmanagement.config.JenkinsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final TestCaseService testCaseService;
    private final PerformanceService performanceExecutionService;
    private final JenkinsConfig jenkinsConfig;
    private final TestResultRepository testResultRepository;

    /**
     * Exécute un test de performance
     * Utilise la configuration Jenkins depuis application.properties
     */
    @PostMapping("/{testCaseId}/run")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
    public ResponseEntity<Map<String,Object>> runPerformance(
            @PathVariable Long testCaseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = testCaseService.getUserIdByUsername(userDetails.getUsername());
        Map<String,Object> resp = performanceExecutionService.triggerPerformanceTest(testCaseId, userId);
        return ResponseEntity.accepted().body(resp);
    }

    /**
     * Exécute un test de performance avec des credentials Jenkins personnalisés
     */
    @PostMapping("/{testCaseId}/run/custom")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
    public ResponseEntity<Map<String, Object>> runPerformanceWithCustomJenkins(
            @PathVariable Long testCaseId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String jenkinsJobUrl,
            @RequestParam String jenkinsUser,
            @RequestParam String jenkinsToken) {

        Long userId = testCaseService.getUserIdByUsername(userDetails.getUsername());
        Map<String, Object> resp = performanceExecutionService.triggerPerformanceTest(
                testCaseId, userId, jenkinsJobUrl, jenkinsUser, jenkinsToken);
        return ResponseEntity.accepted().body(resp);
    }

    /**
     * Callback Jenkins pour recevoir les résultats du test
     */
    @PostMapping("/results/{testResultId}/callback")
    public ResponseEntity<Void> callback(
            @PathVariable Long testResultId,
            @RequestHeader(name = "X-JENKINS-TOKEN", required = true) String token,
            @RequestBody Map<String, Object> metrics) {

        validateToken(token);
        performanceExecutionService.handleJenkinsCallback(testResultId, metrics);
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère les résultats d'un test de performance
     */
    @GetMapping("/results/{testResultId}")
    public ResponseEntity<TestResult> getResult(@PathVariable Long testResultId) {
        TestResult result = performanceExecutionService.getTestResult(testResultId);
        return ResponseEntity.ok(result);
    }

    private void validateToken(String token) {
        String expected = jenkinsConfig.getCallbackToken();
        if (expected == null || !expected.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Jenkins callback token");
        }
    }
 @GetMapping("/test-cases/{testCaseId}/performance-results")
public ResponseEntity<Map<String, Object>> getPerformanceResults(@PathVariable Long testCaseId) {
    TestResult result = testResultRepository.findTopByTestCaseIdOrderByExecutedAtDesc(testCaseId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Aucun résultat de test de performance trouvé pour le test case: " + testCaseId
            ));
    Map<String, Object> response = new HashMap<>();
    response.put("testResult", result);
    response.put("reportUrl", result.getJmeterReportUrl());
    return ResponseEntity.ok(response);
}
}
