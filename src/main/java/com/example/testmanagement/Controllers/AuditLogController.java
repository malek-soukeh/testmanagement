package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.AuditLog;
import com.example.testmanagement.Services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AUDIT')")
    public List<AuditLog> getAuditLogs() {
        return auditLogService.getAllLogs();
    }
}
