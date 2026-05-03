package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.ShiftReport;
import com.bluesky.pos_system.payload.dto.ShiftReportDTO;

public class ShiftReportMapper {
    public static ShiftReportDTO toDTO(ShiftReport shiftReport) {
        return ShiftReportDTO.builder()
                .id(shiftReport.getId())
                .shiftStart(shiftReport.getShiftStart())
                .shiftEnd(shiftReport.getShiftEnd())
                .totalSales(shiftReport.getTotalSales())
                .totalRefunds(shiftReport.getTotalRefunds())
                .totalOrders(shiftReport.getTotalOrders())
                .netSale(shiftReport.getNetSale())
                .cashierId(shiftReport.getCashier().getId())
                .cashier(UserMapper.toDTO(shiftReport.getCashier()))
                .brandId(shiftReport.getBranch().getId())
                .recentOrders(
                        shiftReport.getRecentOrders() != null && !shiftReport.getRecentOrders().isEmpty() ?
                        shiftReport.getRecentOrders().stream().map(OrderMapper::toDTO).toList() : null
                )
                .topSellingProducts(
                        shiftReport.getTopSellingProducts() != null && !shiftReport.getTopSellingProducts().isEmpty() ?
                        shiftReport.getTopSellingProducts().stream().map(ProductMapper::toDTO).toList() : null
                )
                .refunds(
                        shiftReport.getRefunds() != null && !shiftReport.getRefunds().isEmpty() ?
                        shiftReport.getRefunds().stream().map(RefundMapper::toDTO).toList() : null
                )
                .paymentSummaries(shiftReport.getPaymentSummaries())
                .build();
    }
}
