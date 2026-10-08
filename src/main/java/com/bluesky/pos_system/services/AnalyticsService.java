package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;

import java.util.UUID;

public interface AnalyticsService {
    DashboardAnalyticsDTO getDashboardAnalytics(UUID branchId, UUID storeId);
}
