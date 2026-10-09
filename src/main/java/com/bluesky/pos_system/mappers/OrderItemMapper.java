package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.OrderItem;
import com.bluesky.pos_system.payload.dto.OrderItemDTO;

public class OrderItemMapper {
    public static OrderItemDTO toDTO(OrderItem orderItem) {
        if (orderItem == null) return null;
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                .product(orderItem.getProduct() != null ? ProductMapper.toDTO(orderItem.getProduct()) : null)
                .build();
    }
}
