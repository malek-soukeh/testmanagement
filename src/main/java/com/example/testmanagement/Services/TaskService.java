package com.example.testmanagement.Services;

import com.example.testmanagement.DTOs.TaskRequest;
import com.example.testmanagement.DTOs.TaskResponse;
import com.example.testmanagement.Entities.Task;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.TaskRepository;
import com.example.testmanagement.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.testmanagement.Entities.Role;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request, User user) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM);
        task.setCreatedBy(user);

        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            task.setAssignedTo(assignedUser);
        } else {
            // Default assign to creator if not specified, or keep null
            // User requested: "Admin assigns to tester". If not provided, unassigned?
            // "Tester can only see his tasks". If unassigned -> no one sees it except
            // admin.
            // Let's create unassigned if null.
        }

        Task saved = taskRepository.save(task);
        return new TaskResponse(saved);
    }

    public List<TaskResponse> getAllTasks(User currentUser) {
        // Return all tasks for everyone so Testers can see the Team Board.
        // Write access is restricted in updateTask/createTask.
        return taskRepository.findAll().stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isAdmin) {
            // Tester: Can only update if assigned to them
            // AND can only update Status
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access Denied: You can only update tasks assigned to you.");
            }
            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }
            // Ignore other fields or throw error?
            // Ideally simply ignore attempts to change other fields to avoid breaking
            // optimistic UIs
        } else {
            // Admin: Full update
            if (request.getTitle() != null)
                task.setTitle(request.getTitle());
            if (request.getDescription() != null)
                task.setDescription(request.getDescription());
            if (request.getStatus() != null)
                task.setStatus(request.getStatus());
            if (request.getPriority() != null)
                task.setPriority(request.getPriority());

            if (request.getAssignedToUserId() != null) {
                User assignedUser = userRepository.findById(request.getAssignedToUserId())
                        .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                task.setAssignedTo(assignedUser);
            }
        }

        Task updated = taskRepository.save(task);
        return new TaskResponse(updated);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return new TaskResponse(task);
    }
}
