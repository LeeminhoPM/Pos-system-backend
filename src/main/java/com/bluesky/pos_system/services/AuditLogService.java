package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.AuditAction;
import com.bluesky.pos_system.payload.dto.AuditLogDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;

import java.util.UUID;

public interface AuditLogService {
    void logAction(AuditAction action, String entityName, String entityId, String performedBy, String details, String ipAddress);

    PageResponse<AuditLogDTO> getLogs(int page, int size);
}
