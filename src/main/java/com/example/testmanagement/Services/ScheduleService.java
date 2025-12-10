package com.example.testmanagement.Services;

import com.example.testmanagement.DTOs.ScheduleRequest;
import com.example.testmanagement.DTOs.ScheduleResponse;
import com.example.testmanagement.Entities.ScheduledTestExecution;
import com.example.testmanagement.Entities.TestCase;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Entities.Role;
import com.example.testmanagement.Repository.ScheduledTestExecutionRepository;
import com.example.testmanagement.Repository.TestCaseRepository;
import com.example.testmanagement.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduledTestExecutionRepository scheduleRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerformanceService performanceService;

    public ScheduleResponse createSchedule(ScheduleRequest request, User user) {
        System.out.println("Creating schedule with user: " + (user != null ? user.getEmail() : "NULL"));

        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("Test case not found"));

        ScheduledTestExecution schedule = new ScheduledTestExecution();
        schedule.setName(request.getName());
        schedule.setTestCase(testCase);
        schedule.setScheduleType(request.getScheduleType());
        schedule.setActive(request.getActive() != null ? request.getActive() : true);
        schedule.setCreatedBy(user);
        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            // schedule.setAssignedTo(assignedUser);
        }
        schedule.setLastExecutionStatus(ScheduledTestExecution.ExecutionStatus.PENDING);

        LocalDateTime nextExecution = calculateNextExecutionTime(request);
        System.out.println("Calculated next execution time: " + nextExecution + " for schedule: " + request.getName());
        schedule.setNextExecutionTime(nextExecution);

        if (request.getScheduleType() != ScheduledTestExecution.ScheduleType.ONE_TIME) {
            schedule.setCronExpression(generateCronExpression(request));
        }

        ScheduledTestExecution saved = scheduleRepository.save(schedule);
        return new ScheduleResponse(saved);
    }

    public List<ScheduleResponse> getAllSchedules(User currentUser) {
        System.out.println("Fetching schedules for user: " + currentUser.getEmail());
        System.out.println("User Role: " + currentUser.getRole().name());

        boolean hasPrivilegedAccess = (currentUser.getRole() == Role.ROLE_ADMIN
                || currentUser.getRole() == Role.ROLE_TESTER);

        System.out.println("Has privileged access: " + hasPrivilegedAccess);

        if (hasPrivilegedAccess) {
            List<ScheduledTestExecution> allSchedules = scheduleRepository.findAll();
            System.out.println("Total schedules found (Admin/Tester): " + allSchedules.size());
            return allSchedules.stream()
                    .map(ScheduleResponse::new)
                    .collect(Collectors.toList());
        }

        List<ScheduledTestExecution> assignedSchedules = scheduleRepository.findByCreatedBy(currentUser);
        System.out.println("Assigned schedules found: " + assignedSchedules.size());
        return assignedSchedules.stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
    }

    public List<ScheduleResponse> getActiveSchedules() {
        return scheduleRepository.findByActiveOrderByNextExecutionTimeAsc(true).stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
    }

    public ScheduleResponse getScheduleById(Long id) {
        ScheduledTestExecution schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return new ScheduleResponse(schedule);
    }

    public ScheduleResponse updateSchedule(Long id, ScheduleRequest request) {
        ScheduledTestExecution schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (request.getName() != null) {
            schedule.setName(request.getName());
        }
        if (request.getTestCaseId() != null) {
            TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                    .orElseThrow(() -> new RuntimeException("Test case not found"));
            schedule.setTestCase(testCase);
        }
        if (request.getScheduleType() != null) {
            schedule.setScheduleType(request.getScheduleType());
            schedule.setNextExecutionTime(calculateNextExecutionTime(request));
            if (request.getScheduleType() != ScheduledTestExecution.ScheduleType.ONE_TIME) {
                schedule.setCronExpression(generateCronExpression(request));
            }
        }
        if (request.getActive() != null) {
            schedule.setActive(request.getActive());
        }

        ScheduledTestExecution updated = scheduleRepository.save(schedule);
        return new ScheduleResponse(updated);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    public ScheduleResponse toggleSchedule(Long id) {
        ScheduledTestExecution schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setActive(!schedule.getActive());
        ScheduledTestExecution updated = scheduleRepository.save(schedule);
        return new ScheduleResponse(updated);
    }

    @Scheduled(fixedRate = 60000)
    public void executeScheduledTests() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Checking for scheduled tests at: " + now);

        List<ScheduledTestExecution> dueSchedules = scheduleRepository
                .findByNextExecutionTimeBeforeAndActive(now, true);

        System.out.println("Found " + dueSchedules.size() + " scheduled tests to execute");

        for (ScheduledTestExecution schedule : dueSchedules) {
            try {
                System.out.println("Executing scheduled test: " + schedule.getName() +
                        " (ID: " + schedule.getId() + ") for test case: " +
                        schedule.getTestCase().getTitle());

                // Set status to RUNNING and increment execution count
                schedule.setLastExecutionStatus(ScheduledTestExecution.ExecutionStatus.RUNNING);
                schedule.setExecutionCount(schedule.getExecutionCount() + 1);
                scheduleRepository.save(schedule);

                // Send notification: Test started
                sendNotification(schedule, "STARTED", "Test execution started");

                // Execute the test
                boolean success = executeTest(schedule);
                schedule.setLastExecutionTime(now);

                // Update status based on result
                if (success) {
                    schedule.setLastExecutionStatus(ScheduledTestExecution.ExecutionStatus.PASSED);
                    schedule.setSuccessCount(schedule.getSuccessCount() + 1);
                    sendNotification(schedule, "PASSED", "Test execution completed successfully");
                } else {
                    schedule.setLastExecutionStatus(ScheduledTestExecution.ExecutionStatus.FAILED);
                    schedule.setFailureCount(schedule.getFailureCount() + 1);
                    sendNotification(schedule, "FAILED", "Test execution failed");
                }

                if (schedule.getScheduleType() != ScheduledTestExecution.ScheduleType.ONE_TIME) {
                    LocalDateTime nextExecution = calculateNextRecurringTime(schedule);
                    schedule.setNextExecutionTime(nextExecution);
                    System.out.println("Next execution scheduled for: " + nextExecution);
                } else {
                    schedule.setActive(false);
                    System.out.println("One-time schedule completed and deactivated");
                }

                scheduleRepository.save(schedule);
                System.out.println("Successfully executed and updated schedule: " + schedule.getName());
            } catch (Exception e) {
                System.err.println("Error executing scheduled test '" + schedule.getName() + "': " + e.getMessage());
                e.printStackTrace();

                // Mark as failed on exception
                schedule.setLastExecutionStatus(ScheduledTestExecution.ExecutionStatus.FAILED);
                schedule.setFailureCount(schedule.getFailureCount() + 1);
                scheduleRepository.save(schedule);
                sendNotification(schedule, "FAILED", "Test execution error: " + e.getMessage());
            }
        }
    }

    private boolean executeTest(ScheduledTestExecution schedule) {
        TestCase testCase = schedule.getTestCase();

        // Get user ID, use a default if createdBy is null
        Long userId = null;
        if (schedule.getCreatedBy() != null) {
            userId = schedule.getCreatedBy().getId();
        } else {
            System.err
                    .println("Warning: Schedule '" + schedule.getName() + "' has no creator. Using default user ID 1");
            userId = 1L;
        }

        System.out.println("Executing test case: " + testCase.getTitle() + " (Type: " + testCase.getTestType() + ")");

        if (testCase.getTestType() == TestCase.TestType.PERFORMANCE) {
            try {
                System.out.println("Triggering performance test for test case ID: " + testCase.getId());
                performanceService.triggerPerformanceTest(testCase.getId(), userId);
                System.out.println("Performance test triggered successfully");
                return true;
            } catch (Exception e) {
                System.err.println("Error triggering performance test: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else if (testCase.getTestType() == TestCase.TestType.AUTOMATED) {
            System.out.println("Automated test execution not yet implemented for test case: " + testCase.getTitle());
            return false;
        } else {
            System.out.println("Manual tests cannot be scheduled. Skipping test case: " + testCase.getTitle());
            return false;
        }
    }

    private void sendNotification(ScheduledTestExecution schedule, String status, String message) {
        System.out.println("=== NOTIFICATION ===");
        System.out.println("Schedule: " + schedule.getName());
        System.out.println("Test Case: " + schedule.getTestCase().getTitle());
        System.out.println("Status: " + status);
        System.out.println("Message: " + message);
        System.out.println("Time: " + LocalDateTime.now());
        System.out.println("==================");

        // TODO: Implement real notification system (email, WebSocket, push
        // notifications, etc.)
    }

    private LocalDateTime calculateNextExecutionTime(ScheduleRequest request) {
        LocalDateTime now = LocalDateTime.now();

        switch (request.getScheduleType()) {
            case ONE_TIME:
                return request.getExecutionTime();
            case DAILY:
                LocalDateTime dailyTime = now.withHour(request.getHour()).withMinute(request.getMinute()).withSecond(0);
                return dailyTime.isAfter(now) ? dailyTime : dailyTime.plusDays(1);
            case WEEKLY:
                LocalDateTime weeklyTime = now.withHour(request.getHour()).withMinute(request.getMinute())
                        .withSecond(0);
                int currentDayOfWeek = now.getDayOfWeek().getValue();
                int targetDayOfWeek = request.getDayOfWeek();
                int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
                if (daysToAdd == 0 && weeklyTime.isBefore(now)) {
                    daysToAdd = 7;
                }
                return weeklyTime.plusDays(daysToAdd);
            case MONTHLY:
                LocalDateTime monthlyTime = now.withDayOfMonth(request.getDayOfMonth())
                        .withHour(request.getHour()).withMinute(request.getMinute()).withSecond(0);
                return monthlyTime.isAfter(now) ? monthlyTime : monthlyTime.plusMonths(1);
            default:
                return now;
        }
    }

    private LocalDateTime calculateNextRecurringTime(ScheduledTestExecution schedule) {
        LocalDateTime lastExecution = schedule.getLastExecutionTime();

        switch (schedule.getScheduleType()) {
            case DAILY:
                return lastExecution.plusDays(1);
            case WEEKLY:
                return lastExecution.plusWeeks(1);
            case MONTHLY:
                return lastExecution.plusMonths(1);
            default:
                return lastExecution;
        }
    }

    private String generateCronExpression(ScheduleRequest request) {
        switch (request.getScheduleType()) {
            case DAILY:
                return String.format("0 %d %d * * *", request.getMinute(), request.getHour());
            case WEEKLY:
                return String.format("0 %d %d * * %d", request.getMinute(), request.getHour(), request.getDayOfWeek());
            case MONTHLY:
                return String.format("0 %d %d %d * *", request.getMinute(), request.getHour(), request.getDayOfMonth());
            default:
                return null;
        }
    }
}
