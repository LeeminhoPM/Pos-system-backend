package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.AuditLogDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.services.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/audit-logs", "/api/audit-logs"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Audit Logs", description = "APIs for tracking system security and administrative activity logs")
public class AuditLogController {
    AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get system audit logs with pagination")
    public ResponseEntity<PageResponse<AuditLogDTO>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AuditLogDTO> response = auditLogService.getLogs(page, size);
        return ResponseEntity.ok(response);
    }
}
