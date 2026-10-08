package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.OrderItem;
import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsServiceImpl implements AnalyticsService {
    OrderRepository orderRepository;
    ProductRepository productRepository;
    CustomerRepository customerRepository;
    RefundRepository refundRepository;
    InventoryRepository inventoryRepository;

    @Override
    public DashboardAnalyticsDTO getDashboardAnalytics(UUID branchId, UUID storeId) {
        List<Order> orders;
        if (branchId != null) {
            orders = orderRepository.findByBranchId(branchId);
        } else {
            orders = orderRepository.findAll();
        }

        // Filter completed orders for revenue metrics
        List<Order> completedOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();

        double totalRevenue = completedOrders.stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();

        long totalOrders = completedOrders.size();
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
        List<Inventory> inventories;
        if (branchId != null) {
            inventories = inventoryRepository.findByBranchId(branchId);
        } else {
            inventories = inventoryRepository.findAll();
        }
        long lowStockCount = inventories.stream()
                .filter(inv -> {
                    int minStock = (inv.getProduct() != null && inv.getProduct().getMinStockLevel() != null)
                            ? inv.getProduct().getMinStockLevel() : 5;
                    return inv.getQuantity() != null && inv.getQuantity() <= minStock;
                })
                .count();

        // Revenue by Payment Type
        Map<String, Double> revenueByPaymentType = new HashMap<>();
        for (Order o : completedOrders) {
            String pType = o.getPaymentType() != null ? o.getPaymentType().name() : "OTHER";
            revenueByPaymentType.put(pType, revenueByPaymentType.getOrDefault(pType, 0.0) + (o.getTotalAmount() != null ? o.getTotalAmount() : 0.0));
        }

        // Revenue by Cashier
        Map<String, Double> revenueByCashier = new HashMap<>();
        for (Order o : completedOrders) {
            String cashierName = (o.getCashier() != null && o.getCashier().getName() != null)
                    ? o.getCashier().getName() : "Không xác định";
            revenueByCashier.put(cashierName, revenueByCashier.getOrDefault(cashierName, 0.0) + (o.getTotalAmount() != null ? o.getTotalAmount() : 0.0));
        }

        // Top selling products & estimated profit
        Map<UUID, DashboardAnalyticsDTO.TopProductAnalyticsDTO> topProductMap = new HashMap<>();
        double estimatedProfit = 0.0;

        for (Order o : completedOrders) {
            if (o.getItems() == null) continue;
            for (OrderItem item : o.getItems()) {
                if (item.getProduct() == null) continue;
                UUID pId = item.getProduct().getId();
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                double rev = item.getPrice() != null ? item.getPrice() : 0.0;
                double cost = (item.getProduct().getCostPrice() != null ? item.getProduct().getCostPrice() : 0.0) * qty;
                estimatedProfit += (rev - cost);

                DashboardAnalyticsDTO.TopProductAnalyticsDTO existing = topProductMap.get(pId);
                if (existing == null) {
                    existing = DashboardAnalyticsDTO.TopProductAnalyticsDTO.builder()
                            .productId(pId.toString())
                            .productName(item.getProduct().getName())
                            .sku(item.getProduct().getSku())
                            .quantitySold(qty)
                            .revenue(rev)
                            .build();
                } else {
                    existing.setQuantitySold(existing.getQuantitySold() + qty);
                    existing.setRevenue(existing.getRevenue() + rev);
                }
                topProductMap.put(pId, existing);
            }
        }

        List<DashboardAnalyticsDTO.TopProductAnalyticsDTO> topSelling = topProductMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()))
                .limit(5)
                .toList();

        // Daily sales (last 7 days)
        Map<String, double[]> dailyMap = new LinkedHashMap<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = now.minusDays(i);
            dailyMap.put(d.format(df), new double[]{0.0, 0.0}); // [revenue, orderCount]
        }

        for (Order o : completedOrders) {
            if (o.getCreatedAt() != null) {
                String dateKey = o.getCreatedAt().format(df);
                if (dailyMap.containsKey(dateKey)) {
                    double[] vals = dailyMap.get(dateKey);
                    vals[0] += (o.getTotalAmount() != null ? o.getTotalAmount() : 0.0);
                    vals[1] += 1;
                }
            }
        }

        List<DashboardAnalyticsDTO.DailySalesAnalyticsDTO> dailySales = dailyMap.entrySet().stream()
                .map(e -> DashboardAnalyticsDTO.DailySalesAnalyticsDTO.builder()
                        .date(e.getKey())
                        .revenue(e.getValue()[0])
                        .orderCount((long) e.getValue()[1])
                        .build())
                .collect(Collectors.toList());

        return DashboardAnalyticsDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .totalEstimatedProfit(Math.max(0.0, estimatedProfit))
                .totalRefundAmount(totalRefundAmount)
                .totalRefunds(totalRefunds)
                .lowStockAlertCount(lowStockCount)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .revenueByPaymentType(revenueByPaymentType)
                .revenueByCashier(revenueByCashier)
                .topSellingProducts(topSelling)
                .dailySales(dailySales)
                .build();
    }
}
