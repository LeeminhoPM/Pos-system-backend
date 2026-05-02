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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO response =  orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        OrderDTO response =  orderService.getOrderById(id);
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
