package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.ShiftReportDTO;
import com.bluesky.pos_system.services.ShiftReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift-reports")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftReportController {
    ShiftReportService shiftReportService;

    @PostMapping("/start")
    public ResponseEntity<ShiftReportDTO> startShiftReport() {
        ShiftReportDTO response = shiftReportService.startShift();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/end")
    public ResponseEntity<ShiftReportDTO> endShiftReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ShiftReportDTO response = shiftReportService.endShift(endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/current")
    public ResponseEntity<ShiftReportDTO> currentShiftReport() {
        ShiftReportDTO response = shiftReportService.getCurrentShiftReport();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cashier/{cashierId}/date")
    public ResponseEntity<ShiftReportDTO> cashierAndDateShiftReport(
            @PathVariable UUID cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        ShiftReportDTO response = shiftReportService.getShiftReportsByCashierIdAndDate(cashierId, date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<ShiftReportDTO>> cashierShiftReport(@PathVariable UUID cashierId) {
        List<ShiftReportDTO> response = shiftReportService.getShiftReportsByCashierId(cashierId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ShiftReportDTO>> branchShiftReport(@PathVariable UUID branchId) {
        List<ShiftReportDTO> response = shiftReportService.getShiftReportsByBranchId(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftReportDTO> getShiftReportById(@PathVariable UUID id) {
        ShiftReportDTO response = shiftReportService.getShiftReport(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
