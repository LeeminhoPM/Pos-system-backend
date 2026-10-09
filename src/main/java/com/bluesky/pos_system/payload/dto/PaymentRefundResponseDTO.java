package com.bluesky.pos_system.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO kết quả phản hồi hoàn tiền Stripe")
public class PaymentRefundResponseDTO {

    @Schema(description = "Mã hoàn tiền từ Stripe (Refund ID)", example = "re_3Nxyz123456")
    String refundId;

    @Schema(description = "Mã UUID giao dịch nội bộ")
    UUID transactionId;

    @Schema(description = "Mã định danh giao dịch POS", example = "TXN-1234567")
    String transactionCode;

    @Schema(description = "ID đơn hàng liên kết")
    UUID orderId;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261009-001")
    String orderNumber;

    @Schema(description = "Số tiền hoàn trong đợt này", example = "50000.0")
    Double refundedAmount;

    @Schema(description = "Tổng số tiền đã hoàn luỹ kế", example = "50000.0")
    Double totalRefunded;

    @Schema(description = "Trạng thái giao dịch sau khi hoàn (REFUNDED hoặc PARTIALLY_REFUNDED)", example = "REFUNDED")
    String status;

    @Schema(description = "Lý do hoàn tiền")
    String reason;

    @Schema(description = "Thời gian xử lý hoàn tiền")
    LocalDateTime createdAt;
}
