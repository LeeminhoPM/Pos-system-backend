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
public class VietQRPaymentRequestDTO {
    @NotNull(message = "ID đơn hàng không được để trống")
    UUID orderId;

    String bankId;       // MB, VCB, TCB, VPB, ACB, etc. Mặc định MB
    String accountNumber; // Số tài khoản thụ hưởng
    String accountName;   // Tên chủ tài khoản
}
