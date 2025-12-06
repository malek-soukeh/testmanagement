package com.example.testmanagement.Repository;

import com.example.testmanagement.Entities.ScheduledTestExecution;
import com.example.testmanagement.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTestExecutionRepository extends JpaRepository<ScheduledTestExecution, Long> {

    @EntityGraph(attributePaths = { "testCase", "createdBy", "assignedTo" })
    List<ScheduledTestExecution> findByActive(Boolean active);

    @EntityGraph(attributePaths = { "testCase", "createdBy", "assignedTo" })
    List<ScheduledTestExecution> findByCreatedBy(User user);

    @EntityGraph(attributePaths = { "testCase", "createdBy", "assignedTo" })
    List<ScheduledTestExecution> findByAssignedTo(User user);

    @EntityGraph(attributePaths = { "testCase", "createdBy", "assignedTo" })
    List<ScheduledTestExecution> findByNextExecutionTimeBeforeAndActive(LocalDateTime time, Boolean active);

    @EntityGraph(attributePaths = { "testCase", "createdBy", "assignedTo" })
    List<ScheduledTestExecution> findByActiveOrderByNextExecutionTimeAsc(Boolean active);
}
