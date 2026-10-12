package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRPaymentResponseDTO {
    UUID transactionId;
    UUID orderId;
    String orderNumber;
    Double amount;
    String bankId;
    String accountNumber;
    String accountName;
    String transferContent;
    String qrCodeUrl;
    String quickLink;
    String status; // PENDING
}
