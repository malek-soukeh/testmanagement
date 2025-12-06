package com.example.testmanagement.DTOs;

import com.example.testmanagement.Entities.Task;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.Priority priority;
    private Long assignedToUserId;
    private String assignedToUsername;
    private String createdByUsername;
    private LocalDateTime createdAt;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        if (task.getAssignedTo() != null) {
            this.assignedToUserId = task.getAssignedTo().getId();
            this.assignedToUsername = task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName();
        }
        if (task.getCreatedBy() != null) {
            this.createdByUsername = task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName();
        }
        this.createdAt = task.getCreatedAt();
    }
}
