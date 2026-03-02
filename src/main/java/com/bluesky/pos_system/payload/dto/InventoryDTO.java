package com.bluesky.pos_system.payload.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDTO {
    UUID id;

    UUID branchId;

    BranchDTO branch;

    UUID productId;

    ProductDTO product;

    Integer quantity;

    LocalDateTime lastUpdate;
}
