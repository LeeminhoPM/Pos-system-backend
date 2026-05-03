package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.PaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundDTO {
    UUID id;

    UUID orderId;

    OrderDTO order;

    String reason;

    Double amount;

    UUID shiftReportId;

//    ShiftReport shiftReport;

    UserDTO cashier;

    String cashierName;

    UUID branchId;

    BranchDTO branch;

    PaymentType paymentType;

    LocalDateTime createdAt;
}
