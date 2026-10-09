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
@Schema(description = "DTO kết quả phản hồi thanh toán và lịch sử giao dịch")
public class PaymentResponseDTO {

    @Schema(description = "ID bản ghi giao dịch", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id;

    @Schema(description = "Mã định danh giao dịch thanh toán (Transaction Code)", example = "TXN-1234567")
    String transactionId;

    @Schema(description = "Mã đơn hàng liên kết", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID orderId;

    @Schema(description = "Số hiệu đơn hàng", example = "ORD-20261009-001")
    String orderNumber;

    @Schema(description = "Phương thức thanh toán đã chọn", example = "STRIPE")
    String paymentMethod;

    @Schema(description = "Trạng thái giao dịch: PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED, PARTIALLY_REFUNDED", example = "SUCCESS")
    String status;

    @Schema(description = "Client secret từ Stripe Elements dành cho thanh toán thẻ frontend")
    String clientSecret;

    @Schema(description = "Mã tham chiếu Stripe Gateway (PaymentIntent ID)", example = "pi_3Nxyz123456")
    String gatewayReference;

    @Schema(description = "Mã charge Stripe", example = "ch_3Nxyz123456")
    String chargeId;

    @Schema(description = "Đường dẫn biên lai điện tử Stripe", example = "https://pay.stripe.com/receipts/...")
    String receiptUrl;

    @Schema(description = "Đường dẫn trang thanh toán Stripe Checkout")
    String checkoutUrl;

    @Schema(description = "Số tiền thanh toán", example = "150000.0")
    Double amount;

    @Schema(description = "Loại tiền tệ", example = "vnd")
    String currency;

    @Schema(description = "Số tiền đã hoàn", example = "0.0")
    Double refundedAmount;

    @Schema(description = "Thương hiệu thẻ (visa, mastercard, jcb)", example = "visa")
    String cardBrand;

    @Schema(description = "4 số cuối của thẻ", example = "4242")
    String cardLast4;

    @Schema(description = "Email khách hàng")
    String customerEmail;

    @Schema(description = "Ghi chú giao dịch")
    String notes;

    @Schema(description = "Thông báo lỗi nếu giao dịch thất bại")
    String errorMessage;

    @Schema(description = "Thời gian tạo giao dịch")
    LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật giao dịch")
    LocalDateTime updatedAt;
}
