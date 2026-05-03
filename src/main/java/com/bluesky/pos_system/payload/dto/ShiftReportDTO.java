package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.models.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftReportDTO {
    UUID id;

    LocalDateTime shiftStart;

    LocalDateTime shiftEnd;

    Double totalSales;

    Double totalRefunds;

    Double netSale;

    int totalOrders;

    UUID cashierId;

    UserDTO cashier;

    UUID brandId;

    BranchDTO branch;

    List<PaymentSummary> paymentSummaries;

    List<ProductDTO> topSellingProducts;

    List<OrderDTO> recentOrders;

    List<RefundDTO> refunds;
}
