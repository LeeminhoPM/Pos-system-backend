package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.LoyaltyTransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyTransactionDTO {
    UUID id;
    UUID customerId;
    String customerName;
    UUID orderId;
    String orderNumber;
    LoyaltyTransactionType type;
    Integer pointsChange;
    Integer balanceAfter;
    String description;
    LocalDateTime createdAt;
}
