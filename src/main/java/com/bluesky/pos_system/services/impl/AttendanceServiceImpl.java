package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.AttendanceStatus;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.EmployeeAttendance;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.EmployeeAttendanceDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.EmployeeAttendanceRepository;
import com.bluesky.pos_system.services.AttendanceService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceServiceImpl implements AttendanceService {
    EmployeeAttendanceRepository attendanceRepository;
    BranchRepository branchRepository;
    UserService userService;

    @Override
    @Transactional
    public EmployeeAttendanceDTO checkIn(UUID branchId, String notes) {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();

        // Kiểm tra xem đã check-in hôm nay chưa
        var existing = attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today);
        if (existing.isPresent()) {
            throw new IllegalStateException("Bạn đã thực hiện check-in cho ngày hôm nay lúc " + existing.get().getCheckInTime());
        }

        Branch branch = null;
        if (branchId != null) {
            branch = branchRepository.findById(branchId).orElse(null);
        }
        if (branch == null) {
            branch = currentUser.getBranch();
        }
        if (branch == null) {
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh làm việc");
        }

        LocalDateTime now = LocalDateTime.now();
        // Kiểm tra đúng giờ: nếu quá 8:30 sáng thì tính là LATE
        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (now.getHour() >= 8 && now.getMinute() > 30) {
            status = AttendanceStatus.LATE;
        }

        EmployeeAttendance attendance = EmployeeAttendance.builder()
                .user(currentUser)
                .branch(branch)
                .workDate(today)
                .checkInTime(now)
                .status(status)
                .notes(notes)
                .build();

        EmployeeAttendance saved = attendanceRepository.save(attendance);
        log.info("Employee {} checked in at {}: status={}", currentUser.getEmail(), now, status);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmployeeAttendanceDTO checkOut(String notes) {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();

        EmployeeAttendance attendance = attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa thực hiện check-in cho ngày hôm nay"));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Bạn đã thực hiện check-out lúc " + attendance.getCheckOutTime());
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        Duration duration = Duration.between(attendance.getCheckInTime(), now);
        double hours = (double) duration.toMinutes() / 60.0;
        attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);

        if (notes != null && !notes.isBlank()) {
            attendance.setNotes((attendance.getNotes() != null ? attendance.getNotes() + " | " : "") + notes);
        }

        EmployeeAttendance saved = attendanceRepository.save(attendance);
        log.info("Employee {} checked out at {}: workHours={}", currentUser.getEmail(), now, hours);
        return mapToDTO(saved);
    }

    @Override
    public EmployeeAttendanceDTO getTodayAttendance() {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    public List<EmployeeAttendanceDTO> getBranchAttendance(UUID branchId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        return attendanceRepository.findByBranchIdAndWorkDateBetween(branchId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public PageResponse<EmployeeAttendanceDTO> getMyAttendanceHistory(int page, int size) {
        User currentUser = userService.getCurrentUser();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<EmployeeAttendance> pageResult = attendanceRepository.findByUserIdOrderByWorkDateDesc(currentUser.getId(), pageRequest);

        List<EmployeeAttendanceDTO> list = pageResult.getContent().stream().map(this::mapToDTO).toList();
        return PageResponse.<EmployeeAttendanceDTO>builder()
                .content(list)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    private EmployeeAttendanceDTO mapToDTO(EmployeeAttendance a) {
        return EmployeeAttendanceDTO.builder()
                .id(a.getId())
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .employeeName(a.getUser() != null ? a.getUser().getFullName() : null)
                .employeeEmail(a.getUser() != null ? a.getUser().getEmail() : null)
                .branchId(a.getBranch() != null ? a.getBranch().getId() : null)
                .branchName(a.getBranch() != null ? a.getBranch().getName() : null)
                .workDate(a.getWorkDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .workHours(a.getWorkHours())
                .status(a.getStatus())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
