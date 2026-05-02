package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.OrderItem;
import com.bluesky.pos_system.payload.dto.OrderItemDTO;

public class OrderItemMapper {
    public static OrderItemDTO toDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .productId(orderItem.getProduct().getId())
                .product(ProductMapper.toDTO(orderItem.getProduct()))
                .build();
    }
}
