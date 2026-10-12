package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EWalletPaymentResponseDTO {
    UUID transactionId;
    UUID orderId;
    String orderNumber;
    PaymentType walletType;
    Double amount;
    String payUrl;
    String qrCodeUrl;
    String deeplink;
    String status; // PENDING
}
