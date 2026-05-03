package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.ShiftReportDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ShiftReportService {
    ShiftReportDTO startShift();

    ShiftReportDTO endShift(LocalDateTime shiftEnd);

    ShiftReportDTO getShiftReport(UUID id);

    List<ShiftReportDTO> getAllShiftReports();

    List<ShiftReportDTO> getShiftReportsByBranchId(UUID branchId);

    List<ShiftReportDTO> getShiftReportsByCashierId(UUID cashierId);

    ShiftReportDTO getCurrentShiftReport();

    ShiftReportDTO getShiftReportsByCashierIdAndDate(UUID cashierId, LocalDateTime date);
}
