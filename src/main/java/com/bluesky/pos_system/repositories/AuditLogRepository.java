package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.AuditAction;
import com.bluesky.pos_system.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);

    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);

    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}
