package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;
import com.bluesky.pos_system.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/analytics", "/api/analytics"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Analytics & Reports", description = "APIs for dashboard metrics, revenue, sales charts, and top products")
public class AnalyticsController {
    AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get comprehensive POS dashboard analytics")
    public ResponseEntity<DashboardAnalyticsDTO> getDashboardAnalytics(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID storeId
    ) {
        DashboardAnalyticsDTO response = analyticsService.getDashboardAnalytics(branchId, storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/period")
    @Operation(summary = "Thống kê doanh thu và báo cáo theo khoảng thời gian (ngày/tuần/tháng)")
    public ResponseEntity<com.bluesky.pos_system.payload.dto.AnalyticsPeriodReportDTO> getPeriodAnalytics(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate
    ) {
        com.bluesky.pos_system.payload.dto.AnalyticsPeriodReportDTO response = analyticsService.getPeriodAnalytics(branchId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
