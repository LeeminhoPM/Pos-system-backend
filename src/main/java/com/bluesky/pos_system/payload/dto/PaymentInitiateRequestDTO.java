package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO yêu cầu khởi tạo thanh toán đơn hàng")
public class PaymentInitiateRequestDTO {

    @NotNull(message = "ID đơn hàng không được để trống")
    @Schema(description = "Mã UUID đơn hàng cần thanh toán", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID orderId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    @Schema(description = "Số tiền thanh toán (VNĐ)", example = "150000.0")
    Double amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Schema(description = "Phương thức thanh toán", example = "STRIPE")
    PaymentType paymentMethod;

    @Schema(description = "Loại tiền tệ (mặc định VND)", example = "vnd")
    @Builder.Default
    String currency = "vnd";

    @Schema(description = "Email khách hàng nhận hóa đơn điện tử", example = "customer@example.com")
    String customerEmail;

    @Schema(description = "URL chuyển hướng sau khi thanh toán thành công", example = "http://localhost:5173/orders")
    String returnUrl;
}
