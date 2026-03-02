package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.InventoryDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryDTO createInventory(InventoryDTO inventoryDTO);

    InventoryDTO updateInventory(UUID id, InventoryDTO inventoryDTO);

    void deleteInventory(UUID id);

    InventoryDTO getInventoryById(UUID id);

    InventoryDTO getInventoryByProductIdAndBranchId(UUID productId, UUID branchId);

    List<InventoryDTO> getAllInventoryByBranchId(UUID branchId);
}
