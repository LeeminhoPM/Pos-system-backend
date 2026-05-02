package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.payload.dto.OrderDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);

    void deleteOrder(UUID id);

    OrderDTO getOrderById(UUID orderId);

    List<OrderDTO> getOrderByBranch(UUID branchId, UUID customerId, UUID cashierId, PaymentType paymentType, OrderStatus orderStatus);

    List<OrderDTO> getOrderByCashier(UUID cashierId);

    List<OrderDTO> getOrderByCustomer(UUID customerId);

    List<OrderDTO> getTodayOrderByBranch(UUID branchId);

    List<OrderDTO> getTop5RecentOrderByBranch(UUID branchId);
}
