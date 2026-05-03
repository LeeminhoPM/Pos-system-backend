package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.RefundDTO;
import com.bluesky.pos_system.services.RefundService;
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
@RequestMapping("/api/refunds")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundController {
    RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundDTO> createRefund(@RequestBody RefundDTO refundDTO) {
        RefundDTO response = refundService.createRefund(refundDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RefundDTO>> getAllRefunds() {
        List<RefundDTO> response = refundService.getAllRefunds();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<RefundDTO>> getRefundsByCashierId(@PathVariable UUID cashierId) {
        List<RefundDTO> response = refundService.getRefundByCashierId(cashierId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<RefundDTO>> getRefundsByBranchId(@PathVariable UUID branchId) {
        List<RefundDTO> response = refundService.getRefundByBranchId(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<List<RefundDTO>> getRefundsByShiftId(@PathVariable UUID shiftId) {
        List<RefundDTO> response = refundService.getRefundByShiftReportId(shiftId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cashier/{cashierId}/range")
    public ResponseEntity<List<RefundDTO>> getRefundsByCashierIdAndDateRange(
            @PathVariable UUID cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
            ) {
        List<RefundDTO> response = refundService.getRefundByCashierIdAndDateRange(cashierId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundDTO> getRefundById(@PathVariable UUID id) {
        RefundDTO response = refundService.getRefundById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
