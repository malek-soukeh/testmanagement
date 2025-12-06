package com.example.testmanagement.Controllers;

import com.example.testmanagement.DTOs.ActivityDTO;
import com.example.testmanagement.DTOs.DashboardStatsDTO;
import com.example.testmanagement.DTOs.PassRateTrendDTO;
import com.example.testmanagement.DTOs.TestTypeDistributionDTO;
import com.example.testmanagement.Services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDTO>> getActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }

    @GetMapping("/test-type-distribution")
    public ResponseEntity<TestTypeDistributionDTO> getTestTypeDistribution() {
        return ResponseEntity.ok(dashboardService.getTestTypeDistribution());
    }

    @GetMapping("/pass-rate-trend")
    public ResponseEntity<PassRateTrendDTO> getPassRateTrend() {
        return ResponseEntity.ok(dashboardService.getPassRateTrend());
    }

    @GetMapping("/test-execution-velocity")
    public ResponseEntity<com.example.testmanagement.DTOs.TestExecutionVelocityDTO> getTestExecutionVelocity() {
        return ResponseEntity.ok(dashboardService.getTestExecutionVelocity());
    }

    @GetMapping("/tests-by-priority")
    public ResponseEntity<com.example.testmanagement.DTOs.TestsByPriorityDTO> getTestsByPriority() {
        return ResponseEntity.ok(dashboardService.getTestsByPriority());
    }

    @GetMapping("/slowest-tests")
    public ResponseEntity<List<com.example.testmanagement.DTOs.SlowestTestDTO>> getSlowestTests() {
        return ResponseEntity.ok(dashboardService.getSlowestTests());
    }

    @GetMapping("/most-failing-tests")
    public ResponseEntity<List<com.example.testmanagement.DTOs.MostFailingTestDTO>> getMostFailingTests() {
        return ResponseEntity.ok(dashboardService.getMostFailingTests());
    }

    @GetMapping("/automation-progress")
    public ResponseEntity<com.example.testmanagement.DTOs.AutomationProgressDTO> getAutomationProgress() {
        return ResponseEntity.ok(dashboardService.getAutomationProgress());
    }

    @GetMapping("/failed-tests")
    public ResponseEntity<List<com.example.testmanagement.DTOs.FailedTestDTO>> getFailedTests() {
        return ResponseEntity.ok(dashboardService.getFailedTestsRequiringAttention());
    }

    @GetMapping("/upcoming-schedules")
    public ResponseEntity<List<com.example.testmanagement.DTOs.UpcomingScheduleDTO>> getUpcomingScheduledTests() {
        return ResponseEntity.ok(dashboardService.getUpcomingScheduledTests());
    }

    @GetMapping("/schedule-history")
    public ResponseEntity<List<com.example.testmanagement.DTOs.ScheduleExecutionHistoryDTO>> getScheduleExecutionHistory() {
        return ResponseEntity.ok(dashboardService.getScheduleExecutionHistory());
    }

    @GetMapping("/scheduler-stats")
    public ResponseEntity<com.example.testmanagement.DTOs.SchedulerStatsDTO> getSchedulerStatistics() {
        return ResponseEntity.ok(dashboardService.getSchedulerStatistics());
    }
}
