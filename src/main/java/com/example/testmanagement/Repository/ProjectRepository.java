package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.Project;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project,Long> {
    List<Project> findByCreatedBy(User user);
    List<Project> findByProjectNameContainingIgnoreCase(String name);
    List<Project> findByCreatedAtAfter(LocalDateTime date);
    long countByCreatedBy(User user);
}
