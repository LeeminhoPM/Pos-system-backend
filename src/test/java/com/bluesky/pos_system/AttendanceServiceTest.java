package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.AttendanceStatus;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.EmployeeAttendance;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.EmployeeAttendanceDTO;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.EmployeeAttendanceRepository;
import com.bluesky.pos_system.services.UserService;
import com.bluesky.pos_system.services.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private EmployeeAttendanceRepository attendanceRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private User user;
    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = Branch.builder().id(UUID.randomUUID()).name("Chi nhánh 1").build();
        user = User.builder().id(UUID.randomUUID()).email("staff@pos.com").fullName("Le Van C").branch(branch).build();
    }

    @Test
    void checkIn_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(attendanceRepository.findByUserIdAndWorkDate(user.getId(), LocalDate.now())).thenReturn(Optional.empty());
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(attendanceRepository.save(any(EmployeeAttendance.class))).thenAnswer(i -> {
            EmployeeAttendance a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        EmployeeAttendanceDTO result = attendanceService.checkIn(branch.getId(), "Vào ca sáng");

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getCheckInTime());
        verify(attendanceRepository, times(1)).save(any(EmployeeAttendance.class));
    }

    @Test
    void checkIn_alreadyCheckedIn_throwsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        EmployeeAttendance existing = EmployeeAttendance.builder().checkInTime(LocalDateTime.now()).build();
        when(attendanceRepository.findByUserIdAndWorkDate(user.getId(), LocalDate.now())).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> attendanceService.checkIn(branch.getId(), null));
    }

    @Test
    void checkOut_success_calculatesWorkHours() {
        LocalDateTime checkIn = LocalDateTime.now().minusHours(8);
        EmployeeAttendance existing = EmployeeAttendance.builder()
                .id(UUID.randomUUID())
                .user(user)
                .branch(branch)
                .workDate(LocalDate.now())
                .checkInTime(checkIn)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(attendanceRepository.findByUserIdAndWorkDate(user.getId(), LocalDate.now())).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(EmployeeAttendance.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeAttendanceDTO result = attendanceService.checkOut("Tan ca");

        assertNotNull(result);
        assertNotNull(result.getCheckOutTime());
        assertTrue(result.getWorkHours() >= 7.9); // ~8 hours
        verify(attendanceRepository, times(1)).save(existing);
    }
}
