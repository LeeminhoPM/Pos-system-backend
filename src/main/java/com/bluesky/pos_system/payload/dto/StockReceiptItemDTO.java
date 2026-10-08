package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceiptItemDTO {
    UUID id;

    @NotNull(message = "Product ID là bắt buộc")
    UUID productId;

    String productName;

    String sku;

    @NotNull(message = "Số lượng là bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    Integer quantity;

    @NotNull(message = "Đơn giá nhập/xuất là bắt buộc")
    Double unitCost;

    Double totalPrice;
}
