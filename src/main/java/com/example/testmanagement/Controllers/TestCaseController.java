package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Requests.CreateTestCaseRequest;
import com.example.testmanagement.Requests.UpdateTestCaseRequest;
import com.example.testmanagement.Responses.TestCaseResponse;
import com.example.testmanagement.Services.JenkinsService;
import com.example.testmanagement.Services.TestCaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-case/{testsuiteId}")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final JenkinsService jenkinsService;
    private final TestCaseRepository testCaseRepository;

    public TestCaseController(TestCaseService testCaseService, 
                               @Lazy JenkinsService jenkinsService,
                               TestCaseRepository testCaseRepository) {
        this.testCaseService = testCaseService;
        this.jenkinsService = jenkinsService;
        this.testCaseRepository = testCaseRepository;
    }

    @PostMapping
    public ResponseEntity<TestCaseResponse> createTestCase(
            @PathVariable Long testsuiteId,
            @RequestBody CreateTestCaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCase testCase = testCaseService.createTestCase(request, userDetails.getUsername(),testsuiteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(TestCaseResponse.fromEntity(testCase));
    }

    @GetMapping
    public ResponseEntity<List<TestCaseResponse>> getAllTestCases(@PathVariable Long testsuiteId) {
        List<TestCaseResponse> testCases = testCaseService.getAllTestCases(testsuiteId).stream()
                .map(TestCaseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseResponse> getTestCaseById(@PathVariable Long id) {
        TestCase testCase = testCaseService.getTestCaseById(id);
        return ResponseEntity.ok(TestCaseResponse.fromEntity(testCase));
    }

    @GetMapping("/my-test-cases")
    public ResponseEntity<List<TestCaseResponse>> getMyTestCases(@AuthenticationPrincipal UserDetails userDetails) {
        List<TestCaseResponse> testCases = testCaseService.getTestCasesByUser(userDetails.getUsername()).stream()
                .map(TestCaseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/type/{testType}")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByType(@PathVariable TestCase.TestType testType) {
        List<TestCaseResponse> testCases = testCaseService.getTestCasesByType(testType).stream()
                .map(TestCaseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByPriority(@PathVariable TestCase.Priority priority) {
        List<TestCaseResponse> testCases = testCaseService.getTestCasesByPriority(priority).stream()
                .map(TestCaseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TestCaseResponse>> searchTestCases(@RequestParam String title) {
        List<TestCaseResponse> testCases = testCaseService.searchTestCases(title).stream()
                .map(TestCaseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(testCases);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCaseResponse> updateTestCase(
            @PathVariable Long id,
            @RequestBody UpdateTestCaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCase updatedTestCase = testCaseService.updateTestCase(id, request, userDetails.getUsername());
        return ResponseEntity.ok(TestCaseResponse.fromEntity(updatedTestCase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        testCaseService.deleteTestCase(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getTestCaseStatistics() {
        Map<String, Long> statistics = testCaseService.getTestCaseStatistics();
        return ResponseEntity.ok(statistics);
    }

    private Long getUserIdFromUsername(String firstName) {
        return testCaseService.getUserIdByUsername(firstName);
    }
    @PostMapping("/{id}/trigger")
    public ResponseEntity<String> triggerTestCaseExecution(@PathVariable Long id,@PathVariable Long testsuiteId) {
        testCaseService.triggerAutomatedTest(id);
        return ResponseEntity.ok("Test case " + id + " started successfully");
    }

    @GetMapping("/count-by-type")
    public Map<String, Long> getTestCaseCountByType() {
        long manual = testCaseRepository.countByTestType(TestCase.TestType.MANUAL);
        long automated = testCaseRepository.countByTestType(TestCase.TestType.AUTOMATED);
        long performance = testCaseRepository.countByTestType(TestCase.TestType.PERFORMANCE);

        Map<String, Long> response = new HashMap<>();
        response.put("manual", manual);
        response.put("automated", automated);
        response.put("performance", performance);

        return response;
    }

    @GetMapping("/{id}/last-build-result")
    public ResponseEntity<JenkinsService.TestResultSummary> getLastBuildResult(@PathVariable Long id) {
        JenkinsService.TestResultSummary summary = jenkinsService.getLastBuildTestResult();
        if (summary == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        return ResponseEntity.ok(summary);
    }


}
