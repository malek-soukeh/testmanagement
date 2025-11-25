package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Requests.SeleniumCallbackRequest;
import com.example.testmanagement.Services.SeleniumExecutionService;
import com.example.testmanagement.Services.TestCaseService;
import com.example.testmanagement.Services.TestResultService;
import com.example.testmanagement.config.JenkinsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestExecutionController {
    private final TestCaseService testCaseService;
    private final SeleniumExecutionService seleniumExecutionService;
    private final TestResultService testResultService;
    private final JenkinsConfig jenkinsConfig;

    @PostMapping("/{testCaseId}/run")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
    public ResponseEntity<Map<String, Object>> runAutomatedTest(
            @PathVariable Long testCaseId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String jenkinsJobUrl,
            @RequestParam String jenkinsUser,
            @RequestParam String jenkinsToken) {

        Long userId = testCaseService.getUserIdByUsername(userDetails.getUsername());

        Map<String, Object> response = testCaseService.triggerAutomatedTest(
                testCaseId, userId, jenkinsJobUrl, jenkinsUser, jenkinsToken
        );

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/results/{testResultId}/callback")
    public ResponseEntity<Void> handleSeleniumCallback(
            @PathVariable Long testResultId,
            @RequestHeader(name = "X-JENKINS-TOKEN") String token,
            @RequestBody(required = false) SeleniumCallbackRequest payload) {

        validateToken(token);
        seleniumExecutionService.handleSeleniumCallback(testResultId, payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{testCaseId}/latest-result")
    public ResponseEntity<TestResult> getLatestResult(@PathVariable Long testCaseId) {
        return testResultService.getLatestForTestCase(testCaseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/{testResultId}/report")
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable Long testResultId) {
        TestResult result = testResultService.findById(testResultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test result not found"));

        if (result.getExecutionReport() == null || result.getExecutionReport().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No report available for this test");
        }

        byte[] data = result.getExecutionReport().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"selenium-report-" + testResultId + ".json\"")
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }

    private void validateToken(String token) {
        String expected = jenkinsConfig.getCallbackToken();
        if (expected == null || !expected.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Jenkins callback token");
        }
    }
}
