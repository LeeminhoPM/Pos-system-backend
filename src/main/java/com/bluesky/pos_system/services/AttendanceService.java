package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.EmployeeAttendanceDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    EmployeeAttendanceDTO checkIn(UUID branchId, String notes);

    EmployeeAttendanceDTO checkOut(String notes);

    EmployeeAttendanceDTO getTodayAttendance();

    List<EmployeeAttendanceDTO> getBranchAttendance(UUID branchId, LocalDate startDate, LocalDate endDate);

    PageResponse<EmployeeAttendanceDTO> getMyAttendanceHistory(int page, int size);
}
