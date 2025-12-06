package com.example.testmanagement.Services;

import com.example.testmanagement.DTOs.*;
import com.example.testmanagement.Entities.ScheduledTestExecution;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Repository.ProjectRepository;
import com.example.testmanagement.Repository.ScheduledTestExecutionRepository;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private TestCaseRepository testCaseRepository;

        @Autowired
        private TestResultRepository testResultRepository;

        @Autowired
        private ScheduledTestExecutionRepository scheduledTestExecutionRepository;

        public DashboardStatsDTO getStats() {
                long totalProjects = projectRepository.count();
                long totalTestCases = testCaseRepository.count();

                List<TestCase> allTestCases = testCaseRepository.findAll();
                double overallPassRate = 0.0;

                if (!allTestCases.isEmpty()) {
                        long passedTestCases = allTestCases.stream()
                                        .filter(tc -> {
                                                List<TestResult> results = tc.getTestResults();
                                                return !results.isEmpty() &&
                                                                results.get(results.size() - 1)
                                                                                .getStatus() == TestResult.ResultStatus.PASSED;
                                        })
                                        .count();
                        overallPassRate = (double) passedTestCases / allTestCases.size() * 100;
                }

                // Assuming "Critical Issues" are failed tests today
                List<TestResult> allResults = testResultRepository.findAll();
                LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                long criticalIssues = allResults.stream()
                                .filter(r -> r.getStatus() == TestResult.ResultStatus.FAILED
                                                && r.getExecutedAt().isAfter(startOfDay))
                                .count();

                return new DashboardStatsDTO(totalProjects, totalTestCases, Math.round(overallPassRate * 10.0) / 10.0,
                                criticalIssues);
        }

        public List<ActivityDTO> getRecentActivity() {
                return testResultRepository.findAll().stream()
                                .sorted(Comparator.comparing(TestResult::getExecutedAt).reversed())
                                .limit(5)
                                .map(this::mapToActivityDTO)
                                .collect(Collectors.toList());
        }

        private ActivityDTO mapToActivityDTO(TestResult result) {
                String type = result.getStatus() == TestResult.ResultStatus.PASSED ? "pass" : "fail";
                String icon = result.getStatus() == TestResult.ResultStatus.PASSED ? "pi pi-check-circle"
                                : "pi pi-times-circle";
                String message = "Test Run for "
                                + (result.getTestRun() != null && result.getTestRun().getTestCase() != null
                                                ? result.getTestRun().getTestCase().getTitle()
                                                : "Unknown Case")
                                + " " + result.getStatus();

                return new ActivityDTO(type, message, result.getExecutedAt(), icon);
        }

        public TestTypeDistributionDTO getTestTypeDistribution() {
                long manualCount = testCaseRepository.countByTestType(TestCase.TestType.MANUAL);
                long automatedCount = testCaseRepository.countByTestType(TestCase.TestType.AUTOMATED);
                long performanceCount = testCaseRepository.countByTestType(TestCase.TestType.PERFORMANCE);

                return new TestTypeDistributionDTO(
                                Arrays.asList("Manual", "Automated", "Performance"),
                                Arrays.asList(manualCount, automatedCount, performanceCount));
        }

        public PassRateTrendDTO getPassRateTrend() {
                // Mocking trend data for now as complex aggregation requires more
                // time/repository changes
                return new PassRateTrendDTO(
                                Arrays.asList("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"),
                                Arrays.asList(82.0, 85.0, 78.0, 87.0, 90.0, 87.0));
        }

        public TestExecutionVelocityDTO getTestExecutionVelocity() {
                // Get test executions for last 7 days
                LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                List<TestResult> recentResults = testResultRepository.findAll().stream()
                                .filter(r -> r.getExecutedAt().isAfter(sevenDaysAgo))
                                .collect(Collectors.toList());

                // Group by date
                Map<String, Long> countsByDate = new TreeMap<>();
                for (int i = 6; i >= 0; i--) {
                        LocalDateTime date = LocalDateTime.now().minusDays(i);
                        String dateKey = date.toLocalDate().toString();
                        countsByDate.put(dateKey, 0L);
                }

                for (TestResult result : recentResults) {
                        String dateKey = result.getExecutedAt().toLocalDate().toString();
                        countsByDate.put(dateKey, countsByDate.getOrDefault(dateKey, 0L) + 1);
                }

                return new TestExecutionVelocityDTO(
                                new ArrayList<>(countsByDate.keySet()),
                                new ArrayList<>(countsByDate.values()));
        }

        public TestsByPriorityDTO getTestsByPriority() {
                long criticalCount = testCaseRepository.countByPriority(TestCase.Priority.CRITICAL);
                long highCount = testCaseRepository.countByPriority(TestCase.Priority.HIGH);
                long mediumCount = testCaseRepository.countByPriority(TestCase.Priority.MEDIUM);
                long lowCount = testCaseRepository.countByPriority(TestCase.Priority.LOW);

                return new TestsByPriorityDTO(
                                Arrays.asList("Critical", "High", "Medium", "Low"),
                                Arrays.asList(criticalCount, highCount, mediumCount, lowCount));
        }

        public List<SlowestTestDTO> getSlowestTests() {
                // Get all test results and calculate average execution time per test case
                List<TestResult> allResults = testResultRepository.findAll();

                Map<Long, List<Long>> executionTimesByTestCase = new HashMap<>();
                Map<Long, String> testCaseTitles = new HashMap<>();

                for (TestResult result : allResults) {
                        if (result.getTestRun() != null && result.getTestRun().getTestCase() != null) {
                                Long testCaseId = result.getTestRun().getTestCase().getId();
                                Long executionTime = result.getExecutionTimeSeconds() != null
                                                ? result.getExecutionTimeSeconds()
                                                : 0L;

                                executionTimesByTestCase.computeIfAbsent(testCaseId, k -> new ArrayList<>())
                                                .add(executionTime);
                                testCaseTitles.putIfAbsent(testCaseId, result.getTestRun().getTestCase().getTitle());
                        }
                }

                // Calculate averages and create DTOs
                return executionTimesByTestCase.entrySet().stream()
                                .map(entry -> {
                                        Long testCaseId = entry.getKey();
                                        List<Long> times = entry.getValue();
                                        Long avgTime = times.stream().mapToLong(Long::longValue).sum() / times.size();
                                        return new SlowestTestDTO(
                                                        testCaseId,
                                                        testCaseTitles.get(testCaseId),
                                                        avgTime,
                                                        (long) times.size());
                                })
                                .sorted(Comparator.comparing(SlowestTestDTO::getAvgExecutionTime).reversed())
                                .limit(5)
                                .collect(Collectors.toList());
        }

        public List<MostFailingTestDTO> getMostFailingTests() {
                List<TestResult> failedResults = testResultRepository.findAll().stream()
                                .filter(r -> r.getStatus() == TestResult.ResultStatus.FAILED)
                                .collect(Collectors.toList());

                Map<Long, Long> failureCounts = new HashMap<>();
                Map<Long, String> testCaseTitles = new HashMap<>();

                for (TestResult result : failedResults) {
                        if (result.getTestRun() != null && result.getTestRun().getTestCase() != null) {
                                Long id = result.getTestRun().getTestCase().getId();
                                failureCounts.put(id, failureCounts.getOrDefault(id, 0L) + 1);
                                testCaseTitles.putIfAbsent(id, result.getTestRun().getTestCase().getTitle());
                        }
                }

                return failureCounts.entrySet().stream()
                                .map(entry -> new MostFailingTestDTO(
                                                entry.getKey(),
                                                testCaseTitles.get(entry.getKey()),
                                                entry.getValue()))
                                .sorted(Comparator.comparing(MostFailingTestDTO::getFailureCount).reversed())
                                .limit(5)
                                .collect(Collectors.toList());
        }

        public AutomationProgressDTO getAutomationProgress() {
                long totalTests = testCaseRepository.count();
                long automatedTests = testCaseRepository.countByTestType(TestCase.TestType.AUTOMATED);
                long manualTests = testCaseRepository.countByTestType(TestCase.TestType.MANUAL);
                long performanceTests = testCaseRepository.countByTestType(TestCase.TestType.PERFORMANCE);

                return new AutomationProgressDTO(
                                totalTests, automatedTests, manualTests, performanceTests);
        }

        public List<FailedTestDTO> getFailedTestsRequiringAttention() {
                // Get failed tests from last 3 days
                LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
                List<TestResult> recentFailures = testResultRepository.findAll().stream()
                                .filter(r -> r.getStatus() == TestResult.ResultStatus.FAILED
                                                && r.getExecutedAt().isAfter(threeDaysAgo))
                                .sorted(Comparator.comparing(TestResult::getExecutedAt).reversed())
                                .collect(Collectors.toList());

                // Group by test case and count consecutive failures
                Map<Long, List<TestResult>> failuresByTestCase = new HashMap<>();
                for (TestResult result : recentFailures) {
                        if (result.getTestRun() != null && result.getTestRun().getTestCase() != null) {
                                Long testCaseId = result.getTestRun().getTestCase().getId();
                                failuresByTestCase.computeIfAbsent(testCaseId, k -> new ArrayList<>()).add(result);
                        }
                }

                return failuresByTestCase.entrySet().stream()
                                .map(entry -> {
                                        List<TestResult> failures = entry.getValue();
                                        TestResult latest = failures.get(0);
                                        return new FailedTestDTO(
                                                        entry.getKey(),
                                                        latest.getTestRun().getTestCase().getTitle(),
                                                        latest.getExecutedAt(),
                                                        failures.size(),
                                                        latest.getNotes() != null ? latest.getNotes()
                                                                        : "No error message");
                                })
                                .sorted(Comparator.comparing(FailedTestDTO::getConsecutiveFailures).reversed())
                                .limit(10)
                                .collect(Collectors.toList());
        }

        // Scheduler Dashboard Methods

        public List<UpcomingScheduleDTO> getUpcomingScheduledTests() {
                LocalDateTime now = LocalDateTime.now();
                System.out.println("Fetching upcoming schedules at: " + now);
                List<UpcomingScheduleDTO> upcoming = scheduledTestExecutionRepository
                                .findByActiveOrderByNextExecutionTimeAsc(true).stream()
                                .filter(s -> {
                                        boolean isFuture = s.getNextExecutionTime() != null
                                                        && s.getNextExecutionTime().isAfter(now);
                                        if (!isFuture) {
                                                System.out.println("Filtering out schedule: " + s.getName()
                                                                + " with time: " + s.getNextExecutionTime());
                                        }
                                        return isFuture;
                                })
                                .limit(5)
                                .map(UpcomingScheduleDTO::new)
                                .collect(Collectors.toList());
                System.out.println("Found " + upcoming.size() + " upcoming schedules");
                return upcoming;
        }

        public List<ScheduleExecutionHistoryDTO> getScheduleExecutionHistory() {
                return scheduledTestExecutionRepository.findAll().stream()
                                .filter(s -> s.getLastExecutionTime() != null)
                                .sorted(Comparator.comparing(ScheduledTestExecution::getLastExecutionTime).reversed())
                                .limit(10)
                                .map(ScheduleExecutionHistoryDTO::new)
                                .collect(Collectors.toList());
        }

        public SchedulerStatsDTO getSchedulerStatistics() {
                SchedulerStatsDTO stats = new SchedulerStatsDTO();

                List<ScheduledTestExecution> allSchedules = scheduledTestExecutionRepository.findAll();
                List<ScheduledTestExecution> activeSchedules = scheduledTestExecutionRepository.findByActive(true);

                stats.setTotalSchedules((int) scheduledTestExecutionRepository.count());
                stats.setActiveSchedules(activeSchedules.size());

                // Executions today
                LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                int executionsToday = (int) allSchedules.stream()
                                .filter(s -> s.getLastExecutionTime() != null
                                                && s.getLastExecutionTime().isAfter(startOfDay))
                                .count();
                stats.setExecutionsToday(executionsToday);

                // Executions this week
                LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
                int executionsThisWeek = (int) allSchedules.stream()
                                .filter(s -> s.getLastExecutionTime() != null
                                                && s.getLastExecutionTime().isAfter(startOfWeek))
                                .count();
                stats.setExecutionsThisWeek(executionsThisWeek);

                // Success rate
                int totalExecutions = allSchedules.stream()
                                .mapToInt(ScheduledTestExecution::getExecutionCount)
                                .sum();
                int totalSuccesses = allSchedules.stream()
                                .mapToInt(ScheduledTestExecution::getSuccessCount)
                                .sum();

                double successRate = totalExecutions > 0 ? (double) totalSuccesses / totalExecutions * 100 : 0.0;
                stats.setSuccessRate(Math.round(successRate * 10.0) / 10.0);

                // Next execution
                Optional<ScheduledTestExecution> nextSchedule = activeSchedules.stream()
                                .filter(s -> s.getNextExecutionTime() != null)
                                .min(Comparator.comparing(ScheduledTestExecution::getNextExecutionTime));

                if (nextSchedule.isPresent()) {
                        stats.setNextExecution(nextSchedule.get().getNextExecutionTime().toString());
                } else {
                        stats.setNextExecution("No upcoming executions");
                }

                return stats;
        }
}
