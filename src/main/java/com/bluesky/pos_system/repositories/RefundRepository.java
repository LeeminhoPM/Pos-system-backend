package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    List<Refund> findByCashierIdAndCreatedAtBetween(UUID cashierId, LocalDateTime startDate, LocalDateTime endDate);

    List<Refund> findByCashierId(UUID cashierId);

    List<Refund> findByShiftReportId(UUID shiftReportId);

    List<Refund> findByBranchId(UUID branchId);
}
