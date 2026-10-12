package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;

import java.time.LocalDate;
import java.util.UUID;

public interface AnalyticsService {
    DashboardAnalyticsDTO getDashboardAnalytics(UUID branchId, UUID storeId);
    com.bluesky.pos_system.payload.dto.AnalyticsPeriodReportDTO getPeriodAnalytics(UUID branchId, LocalDate startDate, LocalDate endDate);
}
