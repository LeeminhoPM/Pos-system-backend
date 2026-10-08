package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.StockReceiptType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceiptDTO {
    UUID id;

    String receiptNumber;

    @NotNull(message = "Loại phiếu (IN/OUT) là bắt buộc")
    StockReceiptType type;

    @NotNull(message = "Branch ID là bắt buộc")
    UUID branchId;

    String branchName;

    UUID supplierId;

    String supplierName;

    UUID createdById;

    String createdByName;

    Double totalAmount;

    String notes;

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    List<StockReceiptItemDTO> items;

    LocalDateTime createdAt;
}
