package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.AttendanceStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeAttendanceDTO {
    UUID id;
    UUID userId;
    String employeeName;
    String employeeEmail;
    UUID branchId;
    String branchName;
    LocalDate workDate;
    LocalDateTime checkInTime;
    LocalDateTime checkOutTime;
    Double workHours;
    AttendanceStatus status;
    String notes;
    LocalDateTime createdAt;
}
