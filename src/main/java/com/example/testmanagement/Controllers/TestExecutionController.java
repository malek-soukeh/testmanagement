package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Services.JenkinsService;
import com.example.testmanagement.Services.TestCaseService;
import com.example.testmanagement.seleniumrunner.SeleniumRunner;
import com.example.testmanagement.seleniumrunner.SeleniumScenario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class TestExecutionController {

    private final TestCaseService testCaseService;
    private final JenkinsService jenkinsService;
    @PostMapping("/{id}/run")
    public ResponseEntity<Void> runTest(@PathVariable Long id) {
        // Vérifie que le test existe
        testCaseService.getTestCaseById(id);
        // Déclenche Jenkins
        jenkinsService.triggerJenkinsJob(id);
        return ResponseEntity.accepted().build();
    }

}
