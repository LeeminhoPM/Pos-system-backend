package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.payload.dto.PaymentConfigResponseDTO;
import com.bluesky.pos_system.payload.dto.PaymentInitiateRequestDTO;
import com.bluesky.pos_system.payload.dto.PaymentRefundRequestDTO;
import com.bluesky.pos_system.payload.dto.PaymentRefundResponseDTO;
import com.bluesky.pos_system.payload.dto.PaymentResponseDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentConfigResponseDTO getStripeConfig();

    PaymentResponseDTO initiatePayment(PaymentInitiateRequestDTO request);

    PaymentResponseDTO getTransactionStatus(UUID transactionId);

    ApiResponse<String> handleStripeWebhook(String payload, String signatureHeader);

    PaymentRefundResponseDTO refundPayment(PaymentRefundRequestDTO request);

    Page<PaymentResponseDTO> getPaymentHistory(UUID branchId, UUID orderId, PaymentStatus status, PaymentType paymentType, Pageable pageable);

    List<PaymentResponseDTO> getTransactionsByOrder(UUID orderId);
}
