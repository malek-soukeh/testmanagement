package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.Task;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);

    List<Task> findByCreatedBy(User user);
}
