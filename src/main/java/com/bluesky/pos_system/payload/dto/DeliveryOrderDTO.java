package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.DeliveryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryOrderDTO {
    UUID id;
    UUID orderId;
    String orderNumber;
    Double totalAmount;
    String recipientName;
    String recipientPhone;
    String shippingAddress;
    Double shippingFee;
    String trackingNumber;
    String deliveryCarrier;
    DeliveryStatus deliveryStatus;
    LocalDateTime estimatedDeliveryTime;
    LocalDateTime deliveredAt;
    String notes;
    LocalDateTime createdAt;
}
