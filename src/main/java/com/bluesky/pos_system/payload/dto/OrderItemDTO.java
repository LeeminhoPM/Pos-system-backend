package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
