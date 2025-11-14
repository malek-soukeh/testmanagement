package com.example.testmanagement.Controllers;

import com.example.testmanagement.Services.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TESTER')")
public class TestExecutionController {
    private final TestCaseService testCaseService;
    @PostMapping("/{testCaseId}/run")
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
}
