package com.example.testmanagement.Controllers;


import com.example.testmanagement.Entities.TestCaseStep;
import com.example.testmanagement.Requests.CreateTestCaseStepRequest;
import com.example.testmanagement.Requests.UpdateTestCaseStepRequest;
import com.example.testmanagement.Responses.TestCaseStepResponse;
import com.example.testmanagement.Services.TestCaseStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-case-steps")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TESTER')")
public class TestCaseStepController {
    private final TestCaseStepService testCaseStepService;

    @PostMapping
    public ResponseEntity<TestCaseStepResponse> createTestCaseStep(
            @RequestBody CreateTestCaseStepRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCaseStep step = testCaseStepService.createTestCaseStep(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(TestCaseStepResponse.fromEntity(step));
    }

    @PostMapping("/bulk")
    public ResponseEntity<TestCaseStepResponse> createBulkTestCaseStep(
            @RequestBody CreateTestCaseStepRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCaseStep step = testCaseStepService.createBulkTestCaseStep(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(TestCaseStepResponse.fromEntity(step));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseStepResponse> getTestCaseStepById(@PathVariable Long id) {
        TestCaseStep step = testCaseStepService.getTestCaseStepById(id);
        return ResponseEntity.ok(TestCaseStepResponse.fromEntity(step));
    }

    @GetMapping("/test-case/{testCaseId}")
    public ResponseEntity<List<TestCaseStepResponse>> getStepsByTestCaseId(@PathVariable Long testCaseId) {
        List<TestCaseStepResponse> steps = testCaseStepService.getStepsByTestCaseId(testCaseId).stream()
                .map(TestCaseStepResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/test-case/{testCaseId}/result/{result}")
    public ResponseEntity<List<TestCaseStepResponse>> getStepsByTestCaseIdAndResult(
            @PathVariable Long testCaseId,
            @PathVariable TestCaseStep.CaseResult result) {

        List<TestCaseStepResponse> steps = testCaseStepService.getStepsByTestCaseIdAndResult(testCaseId, result).stream()
                .map(TestCaseStepResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/my-steps")
    public ResponseEntity<List<TestCaseStepResponse>> getMySteps(@AuthenticationPrincipal UserDetails userDetails) {
        List<TestCaseStepResponse> steps = testCaseStepService.getStepsByUser(userDetails.getUsername()).stream()
                .map(TestCaseStepResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(steps);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCaseStepResponse> updateTestCaseStep(
            @PathVariable Long id,
            @RequestBody UpdateTestCaseStepRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCaseStep updatedStep = testCaseStepService.updateTestCaseStep(id, request, userDetails.getUsername());
        return ResponseEntity.ok(TestCaseStepResponse.fromEntity(updatedStep));
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<TestCaseStepResponse> updateStepResult(
            @PathVariable Long id,
            @RequestParam TestCaseStep.CaseResult result,
            @RequestParam(required = false) String actualResult,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCaseStep updatedStep = testCaseStepService.updateStepResult(id, result, actualResult, userDetails.getUsername());
        return ResponseEntity.ok(TestCaseStepResponse.fromEntity(updatedStep));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCaseStep(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        testCaseStepService.deleteTestCaseStep(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/test-case/{testCaseId}")
    public ResponseEntity<Void> deleteStepsByTestCaseId(
            @PathVariable Long testCaseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        testCaseStepService.deleteStepsByTestCaseId(testCaseId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-case/{testCaseId}/statistics")
    public ResponseEntity<Map<String, Long>> getStepStatistics(@PathVariable Long testCaseId) {
        Map<String, Long> statistics = testCaseStepService.getStepStatistics(testCaseId);
        return ResponseEntity.ok(statistics);
    }

    @PatchMapping("/test-case/{testCaseId}/reset")
    public ResponseEntity<Void> resetStepResults(
            @PathVariable Long testCaseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        testCaseStepService.resetStepResults(testCaseId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
