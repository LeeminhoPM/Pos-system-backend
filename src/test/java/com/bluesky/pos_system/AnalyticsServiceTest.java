package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.payload.dto.AnalyticsPeriodReportDTO;
import com.bluesky.pos_system.payload.dto.DashboardAnalyticsDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void getDashboardAnalytics_success() {
        when(orderRepository.sumTotalRevenueByBranchAndStatus(any(), eq(OrderStatus.COMPLETED))).thenReturn(15000000.0);
        when(orderRepository.countOrdersByBranchAndStatus(any(), eq(OrderStatus.COMPLETED))).thenReturn(50L);
        when(refundRepository.findAll()).thenReturn(Collections.emptyList());
        when(customerRepository.count()).thenReturn(120L);
        when(productRepository.count()).thenReturn(35L);
        when(inventoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(orderRepository.findPaymentTypeRevenueAggregated(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(orderRepository.findTopProductsAggregated(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(orderRepository.findDailySalesAggregated(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        DashboardAnalyticsDTO result = analyticsService.getDashboardAnalytics(null, null);

        assertNotNull(result);
        assertEquals(15000000.0, result.getTotalRevenue());
        assertEquals(50L, result.getTotalOrders());
        assertEquals(300000.0, result.getAverageOrderValue()); // 15M / 50 = 300k
        assertEquals(120L, result.getTotalCustomers());
    }

    @Test
    void getPeriodAnalytics_success() {
        when(orderRepository.sumRevenueByDateRange(any(), eq(OrderStatus.COMPLETED), any(), any())).thenReturn(5000000.0);
        when(orderRepository.countOrdersByDateRange(any(), eq(OrderStatus.COMPLETED), any(), any())).thenReturn(20L);
        when(orderRepository.findPaymentTypeRevenueAggregated(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(orderRepository.findTopProductsAggregated(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(orderRepository.findDailySalesAggregated(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        AnalyticsPeriodReportDTO result = analyticsService.getPeriodAnalytics(null, LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(result);
        assertEquals(5000000.0, result.getTotalRevenue());
        assertEquals(20L, result.getTotalOrders());
        assertEquals(250000.0, result.getAverageOrderValue());
    }
}
