package com.bluesky.pos_system.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO cấu hình cổng thanh toán Stripe cho frontend")
public class PaymentConfigResponseDTO {

    @Schema(description = "Stripe Publishable Key để khởi tạo Stripe Elements")
    String publishableKey;

    @Schema(description = "Đơn vị tiền tệ mặc định", example = "vnd")
    String currency;

    @Schema(description = "Cờ cho biết hệ thống đang chạy chế độ mô phỏng Sandbox Test", example = "false")
    boolean mockMode;
}
