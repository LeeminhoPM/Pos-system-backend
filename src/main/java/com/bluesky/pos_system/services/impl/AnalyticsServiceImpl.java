package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.payload.dto.AnalyticsPeriodReportDTO;
import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;
import com.bluesky.pos_system.payload.dto.projection.DailySalesProjection;
import com.bluesky.pos_system.payload.dto.projection.PaymentTypeRevenueProjection;
import com.bluesky.pos_system.payload.dto.projection.TopProductProjection;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsServiceImpl implements AnalyticsService {
    OrderRepository orderRepository;
    ProductRepository productRepository;
    CustomerRepository customerRepository;
    RefundRepository refundRepository;
    InventoryRepository inventoryRepository;

    @Override
    public DashboardAnalyticsDTO getDashboardAnalytics(UUID branchId, UUID storeId) {
        // High performance database-level aggregations
        Double totalRevenue = orderRepository.sumTotalRevenueByBranchAndStatus(branchId, OrderStatus.COMPLETED);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrders = orderRepository.countOrdersByBranchAndStatus(branchId, OrderStatus.COMPLETED);
        if (totalOrders == null) totalOrders = 0L;

        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        // Refund metrics
        var refunds = refundRepository.findAll();
        double totalRefundAmount = refunds.stream()
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0.0)
                .sum();
        long totalRefunds = refunds.size();

        // Customer & Product counts
        long totalCustomers = customerRepository.count();
        long totalProducts = productRepository.count();

        // Low stock count
        List<Inventory> inventories = (branchId != null)
                ? inventoryRepository.findByBranchId(branchId)
                : inventoryRepository.findAll();

        long lowStockCount = inventories.stream()
                .filter(inv -> {
                    int minStock = (inv.getProduct() != null && inv.getProduct().getMinStockLevel() != null)
                            ? inv.getProduct().getMinStockLevel() : 5;
                    return inv.getQuantity() != null && inv.getQuantity() <= minStock;
                })
                .count();

        // Time window: Last 7 days
        LocalDateTime startOf7Days = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        // Revenue by Payment Type via DB aggregation
        List<PaymentTypeRevenueProjection> pTypeProjections =
                orderRepository.findPaymentTypeRevenueAggregated(branchId, OrderStatus.COMPLETED, startOf7Days, endOfToday);
        Map<String, Double> revenueByPaymentType = new HashMap<>();
        for (PaymentTypeRevenueProjection p : pTypeProjections) {
            String name = p.getPaymentType() != null ? p.getPaymentType().name() : "OTHER";
            revenueByPaymentType.put(name, p.getTotalRevenue() != null ? p.getTotalRevenue() : 0.0);
        }

        // Top selling products via DB aggregation
        List<TopProductProjection> topProjections =
                orderRepository.findTopProductsAggregated(branchId, OrderStatus.COMPLETED, startOf7Days, endOfToday, PageRequest.of(0, 5));
        List<DashboardAnalyticsDTO.TopProductAnalyticsDTO> topSelling = topProjections.stream()
                .map(tp -> DashboardAnalyticsDTO.TopProductAnalyticsDTO.builder()
                        .productId(tp.getProductId() != null ? tp.getProductId().toString() : "")
                        .productName(tp.getProductName())
                        .sku(tp.getSku())
                        .quantitySold(tp.getTotalQuantitySold() != null ? tp.getTotalQuantitySold().intValue() : 0)
                        .revenue(tp.getTotalRevenue() != null ? tp.getTotalRevenue() : 0.0)
                        .build())
                .toList();

        // Daily sales (last 7 days) via DB aggregation with zero-fill
        List<DailySalesProjection> dailyProjections =
                orderRepository.findDailySalesAggregated(branchId, OrderStatus.COMPLETED, startOf7Days, endOfToday);
        Map<String, DailySalesProjection> projectionMap = new HashMap<>();
        for (DailySalesProjection d : dailyProjections) {
            if (d.getSaleDate() != null) {
                projectionMap.put(d.getSaleDate(), d);
            }
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardAnalyticsDTO.DailySalesAnalyticsDTO> dailySales = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String dateKey = d.format(df);
            DailySalesProjection proj = projectionMap.get(dateKey);
            dailySales.add(DashboardAnalyticsDTO.DailySalesAnalyticsDTO.builder()
                    .date(dateKey)
                    .revenue(proj != null && proj.getTotalRevenue() != null ? proj.getTotalRevenue() : 0.0)
                    .orderCount(proj != null && proj.getOrderCount() != null ? proj.getOrderCount() : 0L)
                    .build());
        }

        return DashboardAnalyticsDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .totalEstimatedProfit(totalRevenue * 0.3) // Ước tính biên lợi nhuận chuẩn
                .totalRefundAmount(totalRefundAmount)
                .totalRefunds(totalRefunds)
                .lowStockAlertCount(lowStockCount)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .revenueByPaymentType(revenueByPaymentType)
                .revenueByCashier(Collections.emptyMap())
                .topSellingProducts(topSelling)
                .dailySales(dailySales)
                .build();
    }

    @Override
    public AnalyticsPeriodReportDTO getPeriodAnalytics(UUID branchId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Double totalRevenue = orderRepository.sumRevenueByDateRange(branchId, OrderStatus.COMPLETED, startDateTime, endDateTime);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrders = orderRepository.countOrdersByDateRange(branchId, OrderStatus.COMPLETED, startDateTime, endDateTime);
        if (totalOrders == null) totalOrders = 0L;

        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        // Payment type breakdown
        List<PaymentTypeRevenueProjection> pTypes =
                orderRepository.findPaymentTypeRevenueAggregated(branchId, OrderStatus.COMPLETED, startDateTime, endDateTime);
        Map<String, Double> paymentMap = new HashMap<>();
        for (PaymentTypeRevenueProjection p : pTypes) {
            String name = p.getPaymentType() != null ? p.getPaymentType().name() : "OTHER";
            paymentMap.put(name, p.getTotalRevenue() != null ? p.getTotalRevenue() : 0.0);
        }

        // Top 10 products
        List<TopProductProjection> topProducts =
                orderRepository.findTopProductsAggregated(branchId, OrderStatus.COMPLETED, startDateTime, endDateTime, PageRequest.of(0, 10));
        List<DashboardAnalyticsDTO.TopProductAnalyticsDTO> topDTOList = topProducts.stream()
                .map(tp -> DashboardAnalyticsDTO.TopProductAnalyticsDTO.builder()
                        .productId(tp.getProductId() != null ? tp.getProductId().toString() : "")
                        .productName(tp.getProductName())
                        .sku(tp.getSku())
                        .quantitySold(tp.getTotalQuantitySold() != null ? tp.getTotalQuantitySold().intValue() : 0)
                        .revenue(tp.getTotalRevenue() != null ? tp.getTotalRevenue() : 0.0)
                        .build())
                .toList();

        // Daily sales breakdown
        List<DailySalesProjection> dailyProjs =
                orderRepository.findDailySalesAggregated(branchId, OrderStatus.COMPLETED, startDateTime, endDateTime);
        List<DashboardAnalyticsDTO.DailySalesAnalyticsDTO> dailyDTOList = dailyProjs.stream()
                .map(d -> DashboardAnalyticsDTO.DailySalesAnalyticsDTO.builder()
                        .date(d.getSaleDate())
                        .revenue(d.getTotalRevenue() != null ? d.getTotalRevenue() : 0.0)
                        .orderCount(d.getOrderCount() != null ? d.getOrderCount() : 0L)
                        .build())
                .toList();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return AnalyticsPeriodReportDTO.builder()
                .startDate(startDate.format(df))
                .endDate(endDate.format(df))
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .revenueByPaymentType(paymentMap)
                .dailySales(dailyDTOList)
                .topSellingProducts(topDTOList)
                .build();
    }
}
