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

    @jakarta.validation.constraints.NotNull(message = "Số lượng không được để trống")
    @jakarta.validation.constraints.Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
    Integer quantity;

    Double price;

    ProductDTO product;

    @jakarta.validation.constraints.NotNull(message = "ID sản phẩm không được để trống")
    UUID productId;

    UUID orderId;
}
