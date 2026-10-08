package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.payload.dto.InventoryDTO;

public class InventoryMapper {
    public static InventoryDTO toDTO(Inventory inventory) {
        if (inventory == null) return null;

        return InventoryDTO.builder()
                .id(inventory.getId())
                .branchId(inventory.getBranch() != null ? inventory.getBranch().getId() : null)
                .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
                .product(inventory.getProduct() != null ? ProductMapper.toDTO(inventory.getProduct()) : null)
                .quantity(inventory.getQuantity())
                .build();
    }

    public static Inventory toEntity(InventoryDTO inventoryDTO, Branch branch, Product product) {
        if (inventoryDTO == null) return null;

        return Inventory.builder()
                .branch(branch)
                .product(product)
                .quantity(inventoryDTO.getQuantity() != null ? inventoryDTO.getQuantity() : 0)
                .build();
    }
}
