package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Responses.TestResultDTO;
import com.example.testmanagement.Services.TestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/test-results")
@RequiredArgsConstructor
public class TestResultController {
    private final TestResultService service;

    @GetMapping
    public List<TestResult> getAllResults() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity saveResult(@RequestBody TestResultDTO dto) {

        log.info("📩 Requête reçue: {}", dto);
        TestResult saved = service.saveFromDto(dto);
        return ResponseEntity.ok(saved);    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TestResult>> getResultsByProject(@PathVariable Long projectId) {
        List<TestResult> results = service.getByProjectId(projectId);
        return ResponseEntity.ok(results);
    }



}
