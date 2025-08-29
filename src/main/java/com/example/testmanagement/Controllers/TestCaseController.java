package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Requests.CreateTestCaseRequest;
import com.example.testmanagement.Requests.UpdateTestCaseRequest;
import com.example.testmanagement.Responses.TestCaseResponse;
import com.example.testmanagement.Services.TestCaseService;
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
@RequestMapping("/api/test-case/{testsuiteId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<TestCaseResponse> createTestCase(
            @RequestBody CreateTestCaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TestCase testCase = testCaseService.createTestCase(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(TestCaseResponse.fromEntity(testCase));
    }

    @GetMapping
    public ResponseEntity<List<TestCaseResponse>> getAllTestCases() {
        List<TestCaseResponse> testCases = testCaseService.getAllTestCases().stream()
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

}
