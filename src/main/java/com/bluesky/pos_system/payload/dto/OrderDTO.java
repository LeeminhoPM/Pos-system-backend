package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Customer;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    UUID id;

    String orderNumber;

    Double subtotal;

    Double discount;

    Double tax;

    Double totalAmount;

    OrderStatus status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    UUID branchId;

    UUID customerId;

    BranchDTO branch;

    UserDTO cashier;

    Customer customer;

    List<OrderItemDTO> items;

    PaymentType paymentType;

    String notes;
}
