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
    @Data
    @AllArgsConstructor
    public class TestReport {
        private Long testCaseId;
        private String testCaseTitle;
        private List<StepResultDto> steps;
    }

    @Data
    @AllArgsConstructor
    public class StepResultDto {
        private String stepName;
        private boolean success;
        private String screenshotBase64;
    }

    private final TestCaseService testCaseService;
    @PostMapping("/{id}/run")
    public ResponseEntity<TestReport> runTest(@PathVariable Long id) throws IOException {
        TestCase testCase = testCaseService.getTestCaseById(id);

        // Convertir TestCase en SeleniumScenario
        SeleniumScenario scenario = testCaseService.buildSeleniumScenario(testCase);

        // Exécuter le scénario
        List<SeleniumRunner.StepResult> results = SeleniumRunner.runScenario(scenario);

        // Préparer DTO pour le frontend
        List<StepResultDto> stepDtos = results.stream()
                .map(r -> new StepResultDto(r.getStepName(), r.isSuccess(), r.getScreenshotBase64()))
                .toList();

        TestReport report = new TestReport(testCase.getId(), testCase.getTitle(), stepDtos);
        return ResponseEntity.ok(report);
    }

}
