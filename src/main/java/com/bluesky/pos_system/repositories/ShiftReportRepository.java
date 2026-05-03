package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.ShiftReport;
import com.bluesky.pos_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftReportRepository extends JpaRepository<ShiftReport, UUID> {
    List<ShiftReport> findByCashierId(UUID cashierId);

    List<ShiftReport> findByBranchId(UUID branchId);

    Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);

    Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime shiftStartAfter, LocalDateTime shiftStartBefore);
}
