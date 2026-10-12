package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EWalletPaymentRequestDTO {
    @NotNull(message = "ID đơn hàng không được để trống")
    UUID orderId;

    @NotNull(message = "Loại ví điện tử không được để trống (MOMO hoặc ZALOPAY)")
    PaymentType walletType; // MOMO hoặc ZALOPAY

    String returnUrl;
}
