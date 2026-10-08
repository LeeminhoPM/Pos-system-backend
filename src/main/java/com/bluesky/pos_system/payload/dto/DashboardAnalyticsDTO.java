package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardAnalyticsDTO {
    Double totalRevenue;
    Long totalOrders;
    Double averageOrderValue;
    Double totalEstimatedProfit;
    Double totalRefundAmount;
    Long totalRefunds;
    Long lowStockAlertCount;
    Long totalProducts;
    Long totalCustomers;
    Map<String, Double> revenueByPaymentType;
    Map<String, Double> revenueByCashier;
    List<TopProductAnalyticsDTO> topSellingProducts;
    List<DailySalesAnalyticsDTO> dailySales;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopProductAnalyticsDTO {
        String productId;
        String productName;
        String sku;
        Integer quantitySold;
        Double revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DailySalesAnalyticsDTO {
        String date; // yyyy-MM-dd
        Double revenue;
        Long orderCount;
    }
}
