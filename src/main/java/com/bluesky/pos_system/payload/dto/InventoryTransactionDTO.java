package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;

    UUID branchId;

    String branchName;

    UUID productId;

    String productName;

    String productSku;

    InventoryTransactionType type;

    Integer quantityChange;

    Integer balanceAfter;

    String referenceNumber;

    String notes;

    UUID createdById;

    String createdByName;

    LocalDateTime createdAt;
}
