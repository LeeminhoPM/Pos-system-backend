package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.InventoryDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryDTO createInventory(InventoryDTO inventoryDTO);

    InventoryDTO updateInventory(UUID id, InventoryDTO inventoryDTO);

    InventoryDTO adjustStock(UUID branchId, UUID productId, Integer deltaQuantity);

    void deleteInventory(UUID id);

    InventoryDTO getInventoryById(UUID id);

    InventoryDTO getInventoryByProductIdAndBranchId(UUID productId, UUID branchId);

    List<InventoryDTO> getAllInventoryByBranchId(UUID branchId);

    List<InventoryDTO> getLowStockByBranchId(UUID branchId);

    InventoryDTO adjustStock(com.bluesky.pos_system.payload.dto.StockAdjustmentRequest request, com.bluesky.pos_system.models.User user);

    com.bluesky.pos_system.payload.dto.PageResponse<com.bluesky.pos_system.payload.dto.InventoryTransactionDTO> getTransactionsByBranch(UUID branchId, int page, int size);

    List<com.bluesky.pos_system.payload.dto.InventoryTransactionDTO> getTransactionsByProduct(UUID productId);

    java.util.Map<String, Object> getLowStockSummary(UUID branchId);
}
