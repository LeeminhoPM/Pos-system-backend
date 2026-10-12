package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmRequestDTO {
    @NotNull(message = "ID giao dịch không được để trống")
    UUID transactionId;

    String bankTransactionReference; // Mã bút toán hoặc mã giao dịch ngân hàng / ví

    String notes;
}
