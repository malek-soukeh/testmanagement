package com.example.testmanagement.Controllers;


import com.example.testmanagement.Entities.TestSuite;
import com.example.testmanagement.Services.TestSuiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-suite/{projectId}")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class TestSuiteController {

    private final TestSuiteService testSuiteService;

    public TestSuiteController(TestSuiteService testSuiteService) {
        this.testSuiteService = testSuiteService;
    }

    @PostMapping
    public ResponseEntity<TestSuite> createSuite(@PathVariable Long projectId,
                                                 @RequestBody TestSuite suite,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                testSuiteService.createTestSuite(projectId, suite, userDetails.getUsername())
        );
    }

    @GetMapping
    public ResponseEntity<List<TestSuite>> getSuites(@PathVariable Long projectId) {
        return ResponseEntity.ok(testSuiteService.getTestSuites(projectId));
    }
}
