package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.*;

import java.util.Map;
import java.util.UUID;

public interface VietnamPaymentService {
    VietQRPaymentResponseDTO createVietQRPayment(VietQRPaymentRequestDTO request);

    EWalletPaymentResponseDTO createEWalletPayment(EWalletPaymentRequestDTO request);

    PaymentResponseDTO confirmPayment(PaymentConfirmRequestDTO request);

    PaymentResponseDTO processWebhook(String gateway, Map<String, Object> webhookData);

    PaymentResponseDTO getTransactionStatus(UUID transactionId);
}
