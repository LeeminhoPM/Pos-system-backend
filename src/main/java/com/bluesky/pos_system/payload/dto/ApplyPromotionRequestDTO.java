package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyPromotionRequestDTO {
    @NotBlank(message = "Mã voucher không được để trống")
    String code;

    @NotNull(message = "Giá trị đơn hàng là bắt buộc")
    Double orderAmount;

    UUID storeId;
}
