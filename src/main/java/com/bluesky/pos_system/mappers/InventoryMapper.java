package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.payload.dto.InventoryDTO;

public class InventoryMapper {
    public static InventoryDTO toDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .branchId(inventory.getBranch().getId())
                .productId(inventory.getProduct().getId())
                .product(ProductMapper.toDTO(inventory.getProduct()))
                .quantity(inventory.getQuantity())
                .build();
    }

    public static Inventory toEntity(InventoryDTO inventoryDTO, Branch branch, Product product) {
        return Inventory.builder()
                .branch(branch)
                .product(product)
                .quantity(inventoryDTO.getQuantity())
                .build();
    }
}
