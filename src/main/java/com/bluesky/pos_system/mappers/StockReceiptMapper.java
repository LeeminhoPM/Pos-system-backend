package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.StockReceipt;
import com.bluesky.pos_system.models.StockReceiptItem;
import com.bluesky.pos_system.payload.dto.StockReceiptDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptItemDTO;

import java.util.ArrayList;
import java.util.List;

public class StockReceiptMapper {
    public static StockReceiptDTO toDTO(StockReceipt receipt) {
        if (receipt == null) return null;

        List<StockReceiptItemDTO> itemDTOs = new ArrayList<>();
        if (receipt.getItems() != null) {
            itemDTOs = receipt.getItems().stream().map(StockReceiptMapper::toItemDTO).toList();
        }

        return StockReceiptDTO.builder()
                .id(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .type(receipt.getType())
                .branchId(receipt.getBranch() != null ? receipt.getBranch().getId() : null)
                .branchName(receipt.getBranch() != null ? receipt.getBranch().getName() : null)
                .supplierId(receipt.getSupplier() != null ? receipt.getSupplier().getId() : null)
                .supplierName(receipt.getSupplier() != null ? receipt.getSupplier().getName() : null)
                .createdById(receipt.getCreatedBy() != null ? receipt.getCreatedBy().getId() : null)
                .createdByName(receipt.getCreatedBy() != null ? receipt.getCreatedBy().getName() : null)
                .totalAmount(receipt.getTotalAmount())
                .notes(receipt.getNotes())
                .items(itemDTOs)
                .createdAt(receipt.getCreatedAt())
                .build();
    }

    public static StockReceiptItemDTO toItemDTO(StockReceiptItem item) {
        if (item == null) return null;

        return StockReceiptItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .sku(item.getProduct() != null ? item.getProduct().getSku() : null)
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
