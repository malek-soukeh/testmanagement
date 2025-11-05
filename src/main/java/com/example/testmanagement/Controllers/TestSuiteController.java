package com.example.testmanagement.Controllers;


import com.example.testmanagement.Entities.TestSuite;
import com.example.testmanagement.Services.TestSuiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{id}")
    public ResponseEntity<Optional<TestSuite>> getTestSuite(@PathVariable Long id)
    {
        return ResponseEntity.ok(testSuiteService.getTestSuite(id));
    }

    @GetMapping
    public ResponseEntity<List<TestSuite>> getSuites(@PathVariable Long projectId) {
        return ResponseEntity.ok(testSuiteService.getTestSuites(projectId));
    }

    @PutMapping("/{Id}")
    public ResponseEntity<TestSuite> updateSuite(@PathVariable Long Id,
                                                 @RequestBody TestSuite suiteDetails,
                                                 @AuthenticationPrincipal UserDetails userDetails)
    {
        TestSuite updatedSuite = testSuiteService.updateTestSuite(Id, suiteDetails, userDetails.getUsername());
        return ResponseEntity.ok(updatedSuite);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestSuite(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        testSuiteService.deleteTestSuite(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
