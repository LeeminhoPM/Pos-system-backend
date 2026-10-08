package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.PaymentInitiateRequestDTO;
import com.bluesky.pos_system.payload.dto.PaymentResponseDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/payments", "/api/payments"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payment & Stripe Gateway", description = "Quản lý cổng thanh toán trực tuyến Stripe và xử lý Webhooks")
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Khởi tạo thanh toán", description = "Tạo phiên thanh toán đơn hàng (CASH, CARD, STRIPE) và sinh clientSecret")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(@Valid @RequestBody PaymentInitiateRequestDTO request) {
        PaymentResponseDTO response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem trạng thái giao dịch thanh toán", description = "Kiểm tra kết quả giao dịch theo mã UUID")
    public ResponseEntity<PaymentResponseDTO> getTransactionStatus(@PathVariable UUID id) {
        PaymentResponseDTO response = paymentService.getTransactionStatus(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/stripe")
    @Operation(summary = "Stripe Webhook Listener", description = "Lắng nghe sự kiện thanh toán từ Stripe server (idempotent)")
    public ResponseEntity<ApiResponse<String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {
        ApiResponse<String> response = paymentService.handleStripeWebhook(payload, signatureHeader);
        return ResponseEntity.ok(response);
    }
}
