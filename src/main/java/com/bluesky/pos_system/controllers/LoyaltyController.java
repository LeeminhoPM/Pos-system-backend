package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/loyalty", "/api/loyalty"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Loyalty & CRM Management", description = "Tích điểm hạng thành viên, quy đổi voucher giảm giá, hồ sơ CRM 360 và phân khúc khách hàng")
public class LoyaltyController {
    LoyaltyService loyaltyService;

    @PostMapping("/redeem")
    @Operation(summary = "Quy đổi điểm thưởng", description = "Quy đổi điểm thưởng của khách hàng sang tiền chiết khấu giảm trực tiếp đơn hàng (1 điểm = 1,000 VND)")
    public ResponseEntity<ApiResponse<RedeemPointsResponseDTO>> redeemPoints(@Valid @RequestBody RedeemPointsRequestDTO request) {
        RedeemPointsResponseDTO response = loyaltyService.redeemPoints(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Quy đổi điểm thưởng thành công"));
    }

    @GetMapping("/customer/{customerId}/history")
    @Operation(summary = "Lịch sử tích và đổi điểm", description = "Tra cứu sổ cái biến động điểm tích lũy của khách hàng có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<LoyaltyTransactionDTO>>> getCustomerLoyaltyHistory(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PageResponse<LoyaltyTransactionDTO> response = loyaltyService.getCustomerLoyaltyHistory(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lịch sử điểm thưởng"));
    }

    @GetMapping("/customer/{customerId}/crm")
    @Operation(summary = "Hồ sơ CRM 360 độ của khách hàng", description = "Xem chi tiết hạng thành viên, giá trị chi tiêu vòng đời, giá trị đơn trung bình và phân khúc")
    public ResponseEntity<ApiResponse<CustomerCRMDetailDTO>> getCustomerCRMDetail(@PathVariable UUID customerId) {
        CustomerCRMDetailDTO response = loyaltyService.getCustomerCRMDetail(customerId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Hồ sơ CRM khách hàng"));
    }

    @GetMapping("/segments")
    @Operation(summary = "Báo cáo tổng quan phân khúc khách hàng", description = "Thống kê số lượng khách hàng theo từng phân hạng BRONZE, SILVER, GOLD, DIAMOND")
    public ResponseEntity<ApiResponse<CustomerSegmentSummaryDTO>> getCustomerSegmentSummary() {
        CustomerSegmentSummaryDTO response = loyaltyService.getCustomerSegmentSummary();
        return ResponseEntity.ok(ApiResponse.ok(response, "Tổng quan phân khúc khách hàng"));
    }
}
