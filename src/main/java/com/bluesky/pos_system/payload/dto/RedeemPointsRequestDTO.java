package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemPointsRequestDTO {
    @NotNull(message = "ID khách hàng không được để trống")
    UUID customerId;

    UUID orderId;

    @NotNull(message = "Số điểm đổi không được để trống")
    @Min(value = 10, message = "Số điểm đổi tối thiểu là 10 điểm")
    Integer pointsToRedeem;
}
