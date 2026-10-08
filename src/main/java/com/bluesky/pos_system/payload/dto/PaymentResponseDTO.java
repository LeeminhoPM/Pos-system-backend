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
@Schema(description = "DTO kết quả phản hồi thanh toán")
public class PaymentResponseDTO {

    @Schema(description = "Mã định danh giao dịch thanh toán (Transaction ID / Intent ID)", example = "pi_3Nxyz123456")
    String transactionId;

    @Schema(description = "Mã đơn hàng liên kết", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID orderId;

    @Schema(description = "Phương thức thanh toán đã chọn", example = "STRIPE")
    String paymentMethod;

    @Schema(description = "Trạng thái giao dịch: PENDING, SUCCESS, FAILED, REQUIRES_ACTION", example = "SUCCESS")
    String status;

    @Schema(description = "Client secret từ Stripe Elements dành cho thanh toán thẻ frontend")
    String clientSecret;

    @Schema(description = "Đường dẫn trang thanh toán Stripe Checkout")
    String checkoutUrl;

    @Schema(description = "Số tiền thanh toán", example = "150000.0")
    Double amount;

    @Schema(description = "Loại tiền tệ", example = "vnd")
    String currency;

    @Schema(description = "Thời gian tạo giao dịch")
    LocalDateTime createdAt;
}
