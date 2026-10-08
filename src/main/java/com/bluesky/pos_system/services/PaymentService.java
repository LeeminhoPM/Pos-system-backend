package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.PaymentInitiateRequestDTO;
import com.bluesky.pos_system.payload.dto.PaymentResponseDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponseDTO initiatePayment(PaymentInitiateRequestDTO request);

    PaymentResponseDTO getTransactionStatus(UUID transactionId);

    ApiResponse<String> handleStripeWebhook(String payload, String signatureHeader);
}
