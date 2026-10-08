package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptDTO;
import com.bluesky.pos_system.services.StockReceiptService;
import com.bluesky.pos_system.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-receipts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Stock Receipts & Inventory History", description = "APIs for warehouse stock-in, stock-out receipts, and inventory tracking")
public class StockReceiptController {
    StockReceiptService stockReceiptService;
    UserService userService;

    @PostMapping
    @Operation(summary = "Create warehouse stock-in or stock-out receipt")
    public ResponseEntity<StockReceiptDTO> createReceipt(@Valid @RequestBody StockReceiptDTO dto) throws Exception {
        User user = userService.getCurrentUser();
        StockReceiptDTO response = stockReceiptService.createStockReceipt(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receipt details by ID")
    public ResponseEntity<StockReceiptDTO> getReceiptById(@PathVariable UUID id) {
        StockReceiptDTO response = stockReceiptService.getStockReceiptById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get all receipts for a branch")
    public ResponseEntity<List<StockReceiptDTO>> getReceiptsByBranch(@PathVariable UUID branchId) {
        List<StockReceiptDTO> response = stockReceiptService.getReceiptsByBranch(branchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/branch/{branchId}")
    @Operation(summary = "Get inventory transaction history for a branch")
    public ResponseEntity<List<InventoryTransactionDTO>> getBranchTransactions(@PathVariable UUID branchId) {
        List<InventoryTransactionDTO> response = stockReceiptService.getInventoryHistory(branchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/product/{productId}")
    @Operation(summary = "Get inventory movement history for a product")
    public ResponseEntity<List<InventoryTransactionDTO>> getProductTransactions(@PathVariable UUID productId) {
        List<InventoryTransactionDTO> response = stockReceiptService.getProductInventoryHistory(productId);
        return ResponseEntity.ok(response);
    }
}
