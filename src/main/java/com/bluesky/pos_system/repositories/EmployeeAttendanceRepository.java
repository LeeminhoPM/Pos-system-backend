package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.EmployeeAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, UUID> {
    Optional<EmployeeAttendance> findByUserIdAndWorkDate(UUID userId, LocalDate workDate);

    List<EmployeeAttendance> findByBranchIdAndWorkDateBetween(UUID branchId, LocalDate start, LocalDate end);

    Page<EmployeeAttendance> findByUserIdOrderByWorkDateDesc(UUID userId, Pageable pageable);
}
