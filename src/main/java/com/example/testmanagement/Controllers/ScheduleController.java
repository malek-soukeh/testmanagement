package com.example.testmanagement.Controllers;

import com.example.testmanagement.DTOs.ScheduleRequest;
import com.example.testmanagement.DTOs.ScheduleResponse;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Services.ScheduleService;
import com.example.testmanagement.Security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ScheduleResponse response = scheduleService.createSchedule(request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ScheduleResponse> schedules = scheduleService.getAllSchedules(userDetails.getUser());
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ScheduleResponse>> getActiveSchedules() {
        List<ScheduleResponse> schedules = scheduleService.getActiveSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        ScheduleResponse schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ScheduleResponse> toggleSchedule(@PathVariable Long id) {
        ScheduleResponse response = scheduleService.toggleSchedule(id);
        return ResponseEntity.ok(response);
    }
}
