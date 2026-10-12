package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.VietnamPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/payments/vietnam", "/api/payments/vietnam"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Vietnam Payment Gateways", description = "Cổng thanh toán MoMo, ZaloPay và chuyển khoản ngân hàng VietQR chuẩn NAPAS 247")
public class VietnamPaymentController {
    VietnamPaymentService vietnamPaymentService;

    @PostMapping("/vietqr")
    @Operation(summary = "Khởi tạo thanh toán chuyển khoản VietQR", description = "Sinh mã VietQR động kèm chính xác số tiền và cú pháp nội dung chuyển khoản theo chuẩn NAPAS 247")
    public ResponseEntity<ApiResponse<VietQRPaymentResponseDTO>> createVietQRPayment(@Valid @RequestBody VietQRPaymentRequestDTO request) {
        VietQRPaymentResponseDTO response = vietnamPaymentService.createVietQRPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Khởi tạo mã VietQR thành công"));
    }

    @PostMapping("/ewallet")
    @Operation(summary = "Khởi tạo thanh toán ví điện tử (MoMo / ZaloPay)", description = "Sinh mã QR và URL thanh toán qua ví điện tử MoMo hoặc ZaloPay")
    public ResponseEntity<ApiResponse<EWalletPaymentResponseDTO>> createEWalletPayment(@Valid @RequestBody EWalletPaymentRequestDTO request) {
        EWalletPaymentResponseDTO response = vietnamPaymentService.createEWalletPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Khởi tạo thanh toán ví điện tử thành công"));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Xác nhận đã nhận tiền thanh toán", description = "Xác nhận hoàn tất giao dịch ngân hàng / ví điện tử, tự động cập nhật đơn hàng thành công và tích điểm cho khách")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> confirmPayment(@Valid @RequestBody PaymentConfirmRequestDTO request) {
        PaymentResponseDTO response = vietnamPaymentService.confirmPayment(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Xác nhận thanh toán thành công"));
    }

    @PostMapping("/webhook/{gateway}")
    @Operation(summary = "Webhook tự động từ Ngân hàng / MoMo / ZaloPay", description = "Lắng nghe tín hiệu IPN xác nhận giao dịch tự động")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> handleWebhook(
            @PathVariable String gateway,
            @RequestBody Map<String, Object> webhookData) {
        PaymentResponseDTO response = vietnamPaymentService.processWebhook(gateway, webhookData);
        return ResponseEntity.ok(ApiResponse.ok(response, "Xử lý webhook thanh toán thành công"));
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Kiểm tra trạng thái thanh toán", description = "Thu ngân POS kiểm tra real-time xem khách hàng đã chuyển tiền thành công chưa")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getTransactionStatus(@PathVariable UUID transactionId) {
        PaymentResponseDTO response = vietnamPaymentService.getTransactionStatus(transactionId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Trạng thái giao dịch"));
    }
}
