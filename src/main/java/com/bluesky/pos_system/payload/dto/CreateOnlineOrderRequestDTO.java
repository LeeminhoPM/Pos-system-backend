package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOnlineOrderRequestDTO {
    @NotNull(message = "Chi nhánh không được để trống")
    UUID branchId;

    UUID customerId;

    @NotBlank(message = "Tên người nhận không được để trống")
    String recipientName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    String recipientPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    String shippingAddress;

    Double shippingFee;

    String deliveryCarrier;

    String notes;

    PaymentType paymentType;

    @NotEmpty(message = "Danh sách sản phẩm mua không được rỗng")
    List<OrderItemDTO> items;
}
