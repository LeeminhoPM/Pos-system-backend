package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.AuditAction;
import com.bluesky.pos_system.mappers.AuditLogMapper;
import com.bluesky.pos_system.models.AuditLog;
import com.bluesky.pos_system.payload.dto.AuditLogDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.repositories.AuditLogRepository;
import com.bluesky.pos_system.services.AuditLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogServiceImpl implements AuditLogService {
    AuditLogRepository auditLogRepository;

    @Override
    public void logAction(AuditAction action, String entityName, String entityId, String performedBy, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public PageResponse<AuditLogDTO> getLogs(int page, int size) {
        Page<AuditLog> pageResult = auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
        List<AuditLogDTO> dtoList = pageResult.getContent().stream()
                .map(AuditLogMapper::toDTO)
                .toList();

        return PageResponse.<AuditLogDTO>builder()
                .content(dtoList)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }
}
