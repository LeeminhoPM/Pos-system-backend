package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.payload.dto.OrderDTO;
import com.bluesky.pos_system.services.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/orders", "/api/orders"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Order Management", description = "Quản lý đơn hàng, chi tiết hóa đơn, chuyển trạng thái và thống kê theo chi nhánh")
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@jakarta.validation.Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO response =  orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        OrderDTO response =  orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {
        OrderDTO response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByBranch(
            @PathVariable UUID branchId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID cashierId,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) OrderStatus orderStatus
            ) {
        List<OrderDTO> response = orderService.getOrderByBranch(branchId, customerId, cashierId, paymentType, orderStatus);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Phân trang và lọc danh sách đơn hàng", description = "Tối ưu hóa hiệu năng cho dữ liệu lớn với pagination và index scan")
    @GetMapping("/branch/{branchId}/paged")
    public ResponseEntity<com.bluesky.pos_system.payload.dto.PageResponse<OrderDTO>> getOrdersPagedByBranch(
            @PathVariable UUID branchId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID cashierId,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        com.bluesky.pos_system.payload.dto.PageResponse<OrderDTO> response = orderService.getOrdersPaged(
                branchId, customerId, cashierId, paymentType, orderStatus, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<OrderDTO>> getOrderByCashier(@PathVariable UUID cashierId) {
        List<OrderDTO> response = orderService.getOrderByCashier(cashierId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getOrderByCustomer(@PathVariable UUID customerId) {
        List<OrderDTO> response = orderService.getOrderByCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/today/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getTodayOrder(@PathVariable UUID branchId) {
        List<OrderDTO> response = orderService.getTodayOrderByBranch(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/recent/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getRecentOrder(@PathVariable UUID branchId) {
        List<OrderDTO> response = orderService.getTop5RecentOrderByBranch(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
