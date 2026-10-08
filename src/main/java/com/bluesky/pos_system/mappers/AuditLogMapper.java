package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.AuditLog;
import com.bluesky.pos_system.payload.dto.AuditLogDTO;

public class AuditLogMapper {
    public static AuditLogDTO toDTO(AuditLog log) {
        if (log == null) return null;

        return AuditLogDTO.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}
