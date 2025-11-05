package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Repository.TestResultRepository;
import com.example.testmanagement.Responses.TestResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestResultService {
    private final TestResultRepository testResultRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TestResult saveFromDto(TestResultDTO dto) {
        TestResult result = new TestResult();
        TestResult.ResultStatus status = dto.getStatus() != null
                ? dto.getStatus()
                : TestResult.ResultStatus.FAILED;
        result.setStatus(status);
        result.setActualResult(dto.getActualResult());
        result.setExecutionTimeSeconds(
                dto.getExecutionTimeSeconds() != null ? dto.getExecutionTimeSeconds() : 0
        );
        result.setExecutedAt(
                dto.getExecutedAt() != null ? dto.getExecutedAt() : java.time.LocalDateTime.now()
        );
        result.setNotes(dto.getNotes());
        result.setTestName(dto.getTestName());
        result.setTestType(dto.getTestType());
        TestResult saved = testResultRepository.save(result);
        messagingTemplate.convertAndSend("/topic/test-results", saved);

        return saved;
    }
    public List<TestResult> getAll() {
        return testResultRepository.findAll();
    }

    public List<TestResult> getByProjectId(Long projectId) {
        return testResultRepository.findByProjectId(projectId);
    }

}
