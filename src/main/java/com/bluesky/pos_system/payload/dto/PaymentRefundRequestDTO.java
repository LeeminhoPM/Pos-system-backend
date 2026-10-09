package com.bluesky.pos_system.payload.dto;

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
@Schema(description = "DTO yêu cầu hoàn tiền giao dịch Stripe")
public class PaymentRefundRequestDTO {

    @NotNull(message = "ID giao dịch không được để trống")
    @Schema(description = "Mã UUID của giao dịch cần hoàn tiền", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID transactionId;

    @Positive(message = "Số tiền hoàn phải lớn hơn 0")
    @Schema(description = "Số tiền cần hoàn (để trống nếu muốn hoàn toàn bộ 100%)", example = "50000.0")
    Double amount;

    @Schema(description = "Lý do hoàn tiền (requested_by_customer, duplicate, fraudulent)", example = "requested_by_customer")
    @Builder.Default
    String reason = "requested_by_customer";
}
