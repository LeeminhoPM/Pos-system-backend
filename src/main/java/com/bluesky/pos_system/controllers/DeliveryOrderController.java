package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.DeliveryStatus;
import com.bluesky.pos_system.payload.dto.DeliveryOrderDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.UpdateDeliveryStatusRequestDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.DeliveryOrderService;
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
@RequestMapping({"/api/v1/deliveries", "/api/deliveries"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Delivery & Shipping Management", description = "Quản lý đơn giao hàng trực tuyến, cập nhật trạng thái vận chuyển và điều phối shipper")
public class DeliveryOrderController {
    DeliveryOrderService deliveryOrderService;

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Danh sách đơn giao hàng theo chi nhánh", description = "Lấy danh sách các đơn giao hàng của chi nhánh có phân trang và lọc theo trạng thái vận chuyển")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryOrderDTO>>> getBranchDeliveryOrders(
            @PathVariable UUID branchId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PageResponse<DeliveryOrderDTO> response = deliveryOrderService.getDeliveryOrdersByBranch(branchId, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, "Danh sách đơn giao hàng"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn giao hàng theo ID")
    public ResponseEntity<ApiResponse<DeliveryOrderDTO>> getDeliveryOrderById(@PathVariable UUID id) {
        DeliveryOrderDTO response = deliveryOrderService.getDeliveryOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Chi tiết đơn giao hàng"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái giao hàng", description = "Chuyển trạng thái đơn vận chuyển: PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> DELIVERED")
    public ResponseEntity<ApiResponse<DeliveryOrderDTO>> updateDeliveryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequestDTO request) {
        DeliveryOrderDTO response = deliveryOrderService.updateDeliveryStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái giao hàng thành công"));
    }
}
