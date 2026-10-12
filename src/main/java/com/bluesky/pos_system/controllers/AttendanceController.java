package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.EmployeeAttendanceDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/attendance", "/api/attendance"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Employee Attendance & Shifts", description = "Chấm công nhân viên, check-in, check-out và theo dõi thời gian làm việc tại các chi nhánh")
public class AttendanceController {
    AttendanceService attendanceService;

    @PostMapping("/check-in")
    @Operation(summary = "Check-in vào ca làm việc", description = "Ghi nhận giờ bắt đầu làm việc của nhân viên tại chi nhánh")
    public ResponseEntity<ApiResponse<EmployeeAttendanceDTO>> checkIn(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String notes) {
        EmployeeAttendanceDTO response = attendanceService.checkIn(branchId, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Check-in thành công"));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check-out kết thúc ca làm việc", description = "Ghi nhận giờ ra về và tự động tính tổng số giờ công làm việc")
    public ResponseEntity<ApiResponse<EmployeeAttendanceDTO>> checkOut(
            @RequestParam(required = false) String notes) {
        EmployeeAttendanceDTO response = attendanceService.checkOut(notes);
        return ResponseEntity.ok(ApiResponse.ok(response, "Check-out thành công"));
    }

    @GetMapping("/today")
    @Operation(summary = "Xem trạng thái chấm công hôm nay của nhân viên hiện tại")
    public ResponseEntity<ApiResponse<EmployeeAttendanceDTO>> getTodayAttendance() {
        EmployeeAttendanceDTO response = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(ApiResponse.ok(response, "Trạng thái công nhật hôm nay"));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Lịch sử chấm công của nhân viên (phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeAttendanceDTO>>> getMyAttendanceHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PageResponse<EmployeeAttendanceDTO> response = attendanceService.getMyAttendanceHistory(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lịch sử chấm công"));
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Xem bảng chấm công nhân sự chi nhánh theo khoảng thời gian")
    public ResponseEntity<ApiResponse<List<EmployeeAttendanceDTO>>> getBranchAttendance(
            @PathVariable UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeAttendanceDTO> response = attendanceService.getBranchAttendance(branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(response, "Danh sách chấm công chi nhánh"));
    }
}
