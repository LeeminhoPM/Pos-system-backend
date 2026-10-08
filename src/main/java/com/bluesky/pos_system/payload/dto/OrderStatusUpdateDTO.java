package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO cập nhật trạng thái đơn hàng")
public class OrderStatusUpdateDTO {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    @Schema(description = "Trạng thái mới: PENDING, PROCESSING, COMPLETED, CANCELLED, REFUNDED", example = "COMPLETED")
    OrderStatus status;

    @Schema(description = "Lý do cập nhật hoặc ghi chú hủy/hoàn đơn", example = "Khách hàng thanh toán hoàn tất")
    String reason;
}
