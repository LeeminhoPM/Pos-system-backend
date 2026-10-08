package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.OrderDTO;

import java.util.Collections;

public class OrderMapper {
    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : null)
                .branch(order.getBranch() != null ? BranchMapper.toDTO(order.getBranch()) : null)
                .cashier(order.getCashier() != null ? UserMapper.toDTO(order.getCashier()) : null)
                .customer(order.getCustomer())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .paymentType(order.getPaymentType())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() != null ? order.getItems().stream().map(OrderItemMapper::toDTO).toList() : Collections.emptyList())
                .build();
    }
}
