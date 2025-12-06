package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.Task.Priority;
import com.example.testmanagement.Entities.Task.TaskStatus;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Long assignedToUserId;
}
