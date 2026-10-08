package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.AuditAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogDTO {
    UUID id;
    AuditAction action;
    String entityName;
    String entityId;
    String performedBy;
    String details;
    String ipAddress;
    LocalDateTime timestamp;
}
