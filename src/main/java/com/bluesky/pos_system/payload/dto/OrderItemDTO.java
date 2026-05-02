package com.bluesky.pos_system.payload.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemDTO {
    UUID id;

    Integer quantity;

    Double price;

    ProductDTO product;

    UUID productId;

    UUID orderId;
}
