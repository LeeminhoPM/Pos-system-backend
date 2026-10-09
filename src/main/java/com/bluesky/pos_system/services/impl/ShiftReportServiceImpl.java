package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.mappers.ShiftReportMapper;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.ShiftReportDTO;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.RefundRepository;
import com.bluesky.pos_system.repositories.ShiftReportRepository;
import com.bluesky.pos_system.repositories.UserRepository;
import com.bluesky.pos_system.services.ShiftReportService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftReportServiceImpl implements ShiftReportService {
    ShiftReportRepository shiftReportRepository;
    RefundRepository refundRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    UserService userService;

    @Override
    @Transactional
    public ShiftReportDTO startShift() {
        User cashier = userService.getCurrentUser();
        LocalDateTime shiftStart = LocalDateTime.now();

        // Check if there is an unended shift currently
        Optional<ShiftReport> ongoingShift = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier);
        if (ongoingShift.isPresent()) {
            throw new RuntimeException("Bạn đang có một ca làm việc chưa kết thúc. Vui lòng kết thúc ca làm hiện tại trước!");
        }

        Branch branch = cashier.getBranch();
        ShiftReport shiftReport = ShiftReport.builder()
                .cashier(cashier)
                .branch(branch)
                .shiftStart(shiftStart)
                .totalSales(0.0)
                .totalRefunds(0.0)
                .netSale(0.0)
                .totalOrders(0)
                .build();
        return ShiftReportMapper.toDTO(shiftReportRepository.save(shiftReport));
    }

    @Override
    @Transactional
    public ShiftReportDTO endShift(LocalDateTime shiftEnd) {
        User cashier = userService.getCurrentUser();
        ShiftReport shiftReport = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy ca làm việc đang mở để kết thúc")
        );
        LocalDateTime actualEnd = shiftEnd != null ? shiftEnd : LocalDateTime.now();
        shiftReport.setShiftEnd(actualEnd);

        List<Refund> refunds = refundRepository.findByCashierIdAndCreatedAtBetween(
                cashier.getId(), shiftReport.getShiftStart(), actualEnd
        );
        List<Order> orders = orderRepository.findByCashierIdAndCreatedAtBetween(
                cashier.getId(), shiftReport.getShiftStart(), actualEnd
        );

        return getShiftReportDTO(shiftReport, refunds, orders);
    }

    @Override
    public ShiftReportDTO getShiftReport(UUID id) {
        ShiftReport shiftReport = shiftReportRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy ca làm")
        );
        return ShiftReportMapper.toDTO(shiftReport);
    }

    @Override
    public List<ShiftReportDTO> getAllShiftReports() {
        List<ShiftReport> shiftReports = shiftReportRepository.findAll();
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByBranchId(UUID branchId) {
        List<ShiftReport> shiftReports = shiftReportRepository.findByBranchId(branchId);
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByCashierId(UUID cashierId) {
        List<ShiftReport> shiftReports = shiftReportRepository.findByCashierId(cashierId);
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShiftReportDTO getCurrentShiftReport() {
        User cashier = userService.getCurrentUser();
        Optional<ShiftReport> shiftReportOpt = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier);
        if (shiftReportOpt.isEmpty()) {
            return null;
        }
        ShiftReport shiftReport = shiftReportOpt.get();
        LocalDateTime now = LocalDateTime.now();

        List<Refund> refunds = refundRepository.findByCashierIdAndCreatedAtBetween(
                cashier.getId(), shiftReport.getShiftStart(), now
        );
        List<Order> orders = orderRepository.findByCashierIdAndCreatedAtBetween(
                cashier.getId(), shiftReport.getShiftStart(), now
        );

        return getShiftReportDTO(shiftReport, refunds, orders);
    }

    @Override
    public ShiftReportDTO getShiftReportsByCashierIdAndDate(UUID cashierId, LocalDateTime date) {
        User cashier = userRepository.findById(cashierId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhân viên")
        );
        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);

        ShiftReport shiftReport = shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy ca làm")
        );
        return ShiftReportMapper.toDTO(shiftReport);
    }

    private ShiftReportDTO getShiftReportDTO(ShiftReport shiftReport, List<Refund> refunds, List<Order> orders) {
        double totalRefunds = refunds.stream().mapToDouble(
                refund -> refund.getAmount() != null ? refund.getAmount() : 0.0
        ).sum();
        double totalSales = orders.stream().mapToDouble(
                order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0.0
        ).sum();
        int totalOrders = orders.size();
        double netSales = totalSales - totalRefunds;

        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalRefunds(totalRefunds);
        shiftReport.setTotalOrders(totalOrders);
        shiftReport.setNetSale(netSales);
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
        shiftReport.setRefunds(refunds);

        return ShiftReportMapper.toDTO(shiftReportRepository.save(shiftReport));
    }

    private List<PaymentSummary> getPaymentSummaries(List<Order> orders, double totalSales) {
        Map<PaymentType, List<Order>> grouped = orders.stream().collect(Collectors.groupingBy(
                order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH
        ));
        List<PaymentSummary> paymentSummaries = new ArrayList<>();
        for (Map.Entry<PaymentType, List<Order>> entry : grouped.entrySet()) {
            double amount = entry.getValue().stream().mapToDouble(
                    o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0
            ).sum();
            int transactions = entry.getValue().size();
            double percentage = totalSales > 0 ? (amount * 100.0 / totalSales) : 0.0;

            PaymentSummary paymentSummary = new PaymentSummary();
            paymentSummary.setPaymentType(entry.getKey());
            paymentSummary.setTotalAmount(amount);
            paymentSummary.setTransactionCount(transactions);
            paymentSummary.setPercentage(percentage);
            paymentSummaries.add(paymentSummary);
        }
        return paymentSummaries;
    }

    private List<Product> getTopSellingProducts(List<Order> orders) {
        Map<Product, Integer> productSalesMap = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product != null) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                        productSalesMap.put(product, productSalesMap.getOrDefault(product, 0) + qty);
                    }
                }
            }
        }
        return productSalesMap.entrySet().stream().sorted(
                (a, b) -> b.getValue().compareTo(a.getValue())
        ).limit(5).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private List<Order> getRecentOrders(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}
