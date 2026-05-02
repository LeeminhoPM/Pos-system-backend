package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.OrderDTO;

public class OrderMapper {
    public static OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .branchId(order.getBranch().getId())
                .cashier(UserMapper.toDTO(order.getCashier()))
                .customer(order.getCustomer())
                .paymentType(order.getPaymentType())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(OrderItemMapper::toDTO).toList())
                .build();
    }
}
