package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Customer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    UUID id;

    Double totalAmount;

    LocalDateTime createdAt;

    UUID branchId;

    UUID customerId;

    BranchDTO branch;

    UserDTO cashier;

    Customer customer;

    List<OrderItemDTO> items;

    PaymentType paymentType;
}
