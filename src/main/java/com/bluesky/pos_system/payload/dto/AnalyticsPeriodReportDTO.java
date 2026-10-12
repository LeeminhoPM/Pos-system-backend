package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyticsPeriodReportDTO {
    String startDate;
    String endDate;
    Double totalRevenue;
    Long totalOrders;
    Double averageOrderValue;
    Map<String, Double> revenueByPaymentType;
    List<DashboardAnalyticsDTO.DailySalesAnalyticsDTO> dailySales;
    List<DashboardAnalyticsDTO.TopProductAnalyticsDTO> topSellingProducts;
}
