package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_attendance", indexes = {
        @Index(name = "idx_att_user_date", columnList = "user_id, work_date"),
        @Index(name = "idx_att_branch", columnList = "branch_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    Branch branch;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    LocalDateTime checkInTime;

    LocalDateTime checkOutTime;

    Double workHours;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    AttendanceStatus status;

    String notes;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
}
