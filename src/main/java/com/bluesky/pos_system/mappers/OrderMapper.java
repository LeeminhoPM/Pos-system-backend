package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.OrderDTO;

import java.util.Collections;
import java.util.UUID;

public class OrderMapper {
    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        com.bluesky.pos_system.payload.dto.BranchDTO branchDTO = null;
        UUID branchId = null;
        try {
            if (order.getBranch() != null) {
                branchId = order.getBranch().getId();
                branchDTO = BranchMapper.toDTO(order.getBranch());
            }
        } catch (Exception ignored) {}

        com.bluesky.pos_system.payload.dto.UserDTO cashierDTO = null;
        try {
            if (order.getCashier() != null) {
                cashierDTO = UserMapper.toDTO(order.getCashier());
            }
        } catch (Exception ignored) {}

        UUID customerId = null;
        try {
            if (order.getCustomer() != null) {
                customerId = order.getCustomer().getId();
            }
        } catch (Exception ignored) {}

        java.util.List<com.bluesky.pos_system.payload.dto.OrderItemDTO> itemDTOs = Collections.emptyList();
        try {
            if (order.getItems() != null) {
                itemDTOs = order.getItems().stream().map(OrderItemMapper::toDTO).toList();
            }
        } catch (Exception ignored) {}

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .branchId(branchId)
                .branch(branchDTO)
                .cashier(cashierDTO)
                .customer(order.getCustomer())
                .customerId(customerId)
                .paymentType(order.getPaymentType())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
}
