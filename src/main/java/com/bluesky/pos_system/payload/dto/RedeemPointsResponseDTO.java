package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemPointsResponseDTO {
    UUID customerId;
    String customerName;
    Integer redeemedPoints;
    Double discountAmount; // 1 point = 1,000 VND
    Integer remainingPoints;
    String message;
}
