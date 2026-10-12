package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateDeliveryStatusRequestDTO {
    @NotNull(message = "Trạng thái giao hàng không được để trống")
    DeliveryStatus status;

    String trackingNumber;

    String notes;
}
