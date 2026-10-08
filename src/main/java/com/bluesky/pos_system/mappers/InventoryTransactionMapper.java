package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.InventoryTransaction;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;

public class InventoryTransactionMapper {
    public static InventoryTransactionDTO toDTO(InventoryTransaction tx) {
        if (tx == null) return null;

        return InventoryTransactionDTO.builder()
                .id(tx.getId())
                .branchId(tx.getBranch() != null ? tx.getBranch().getId() : null)
                .branchName(tx.getBranch() != null ? tx.getBranch().getName() : null)
                .productId(tx.getProduct() != null ? tx.getProduct().getId() : null)
                .productName(tx.getProduct() != null ? tx.getProduct().getName() : null)
                .productSku(tx.getProduct() != null ? tx.getProduct().getSku() : null)
                .type(tx.getType())
                .quantityChange(tx.getQuantityChange())
                .balanceAfter(tx.getBalanceAfter())
                .referenceNumber(tx.getReferenceNumber())
                .notes(tx.getNotes())
                .createdById(tx.getCreatedBy() != null ? tx.getCreatedBy().getId() : null)
                .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getFullName() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
