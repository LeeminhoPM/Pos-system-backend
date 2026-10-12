package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.CustomerTier;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCRMDetailDTO {
    UUID id;
    String customerCode;
    String fullName;
    String email;
    String phone;
    String address;
    CustomerTier tier;
    LocalDate dateOfBirth;
    String gender;
    Integer loyaltyPoints;
    Double totalSpent;
    Long totalOrders;
    Double averageOrderValue;
    String segmentName; // VIP, HIGH_VALUE, REGULAR, NEW, DORMANT
    LocalDateTime createdAt;
    LocalDateTime lastOrderDate;
    List<LoyaltyTransactionDTO> recentLoyaltyTransactions;
    List<OrderDTO> recentOrders;
}
