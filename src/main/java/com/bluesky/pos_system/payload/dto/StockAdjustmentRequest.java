package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Chi nhánh không được để trống")
    UUID branchId;

    @NotNull(message = "Sản phẩm không được để trống")
    UUID productId;

    @NotNull(message = "Số lượng điều chỉnh không được để trống")
    Integer quantityChange;

    InventoryTransactionType type; // ADJUSTMENT, STOCK_IN, STOCK_OUT, RETURN

    String reason; // e.g., "AUDIT_COUNT", "DAMAGED", "EXPIRED", "SUPPLIER_RESTOCK"

    String notes;
}
