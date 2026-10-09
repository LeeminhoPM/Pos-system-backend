package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping({"/api/v1/payments", "/api/payments"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payment & Stripe Gateway", description = "Quản lý cổng thanh toán trực tuyến Stripe, Webhooks, Hoàn tiền và Lịch sử giao dịch")
public class PaymentController {

    PaymentService paymentService;

    @GetMapping("/config")
    @Operation(summary = "Lấy cấu hình Stripe Gateway", description = "Lấy Publishable Key và đơn vị tiền tệ để khởi tạo Stripe Elements")
    public ResponseEntity<ApiResponse<PaymentConfigResponseDTO>> getStripeConfig() {
        PaymentConfigResponseDTO config = paymentService.getStripeConfig();
        return ResponseEntity.ok(ApiResponse.ok(config, "Cấu hình Stripe Gateway"));
    }

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

    @PostMapping("/refund")
    @Operation(summary = "Hoàn tiền giao dịch", description = "Thực hiện hoàn tiền một phần hoặc toàn phần qua Stripe API")
    public ResponseEntity<ApiResponse<PaymentRefundResponseDTO>> refundPayment(
            @Valid @RequestBody PaymentRefundRequestDTO request) {
        PaymentRefundResponseDTO response = paymentService.refundPayment(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Hoàn tiền thành công"));
    }

    @GetMapping("/history")
    @Operation(summary = "Lịch sử giao dịch thanh toán", description = "Truy vấn danh sách lịch sử giao dịch có phân trang và bộ lọc")
    public ResponseEntity<ApiResponse<Page<PaymentResponseDTO>>> getPaymentHistory(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentResponseDTO> history = paymentService.getPaymentHistory(branchId, orderId, status, paymentType, pageable);
        return ResponseEntity.ok(ApiResponse.ok(history, "Danh sách lịch sử giao dịch"));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Danh sách giao dịch theo đơn hàng", description = "Lấy toàn bộ các giao dịch liên kết với một đơn hàng cụ thể")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getTransactionsByOrder(@PathVariable UUID orderId) {
        List<PaymentResponseDTO> transactions = paymentService.getTransactionsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(transactions, "Giao dịch của đơn hàng"));
    }

    @PostMapping("/webhook/stripe")
    @Operation(summary = "Stripe Webhook Listener", description = "Lắng nghe và xác thực chữ ký số sự kiện thanh toán từ Stripe server (idempotent)")
    public ResponseEntity<ApiResponse<String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {
        try {
            ApiResponse<String> response = paymentService.handleStripeWebhook(payload, signatureHeader);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Stripe webhook verification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(), 400));
        }
    }
}
