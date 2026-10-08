package com.bluesky.pos_system.services;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptDTO;

import java.util.List;
import java.util.UUID;

public interface StockReceiptService {
    StockReceiptDTO createStockReceipt(StockReceiptDTO dto, User user);

    StockReceiptDTO getStockReceiptById(UUID id);

    List<StockReceiptDTO> getReceiptsByBranch(UUID branchId);

    List<InventoryTransactionDTO> getInventoryHistory(UUID branchId);

    List<InventoryTransactionDTO> getProductInventoryHistory(UUID productId);
}
