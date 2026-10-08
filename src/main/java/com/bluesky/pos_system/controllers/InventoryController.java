package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.InventoryDTO;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.StockAdjustmentRequest;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.InventoryService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/inventories", "/api/inventories"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Inventory Management", description = "Quản lý kho hàng, điều chỉnh tồn kho, kiểm kê, cảnh báo sắp hết hàng và lịch sử biến động kho")
public class InventoryController {
    InventoryService inventoryService;
    UserService userService;

    @Operation(summary = "Tạo mới hoặc khởi tạo bản ghi tồn kho")
    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO response = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật số lượng tồn kho")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable UUID id, @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO response = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Điều chỉnh tồn kho với lý do và lưu lịch sử kiểm toán")
    @PostMapping("/adjust")
    public ResponseEntity<InventoryDTO> adjustStockWithAudit(@Valid @RequestBody StockAdjustmentRequest request) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception ignored) {
        }
        InventoryDTO response = inventoryService.adjustStock(request, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Điều chỉnh nhanh số lượng tồn kho qua query params")
    @PostMapping("/adjust-quick")
    public ResponseEntity<InventoryDTO> adjustStockQuick(
            @RequestParam UUID branchId,
            @RequestParam UUID productId,
            @RequestParam Integer deltaQuantity
    ) {
        InventoryDTO response = inventoryService.adjustStock(branchId, productId, deltaQuantity);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Xóa bản ghi tồn kho")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteInventory(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Xóa thành công"));
    }

    @Operation(summary = "Lấy toàn bộ danh sách tồn kho theo chi nhánh")
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByBranchId(@PathVariable UUID branchId) {
        List<InventoryDTO> response = inventoryService.getAllInventoryByBranchId(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lấy danh sách các mặt hàng sắp hết hoặc đã hết tồn kho")
    @GetMapping("/branch/{branchId}/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockByBranchId(@PathVariable UUID branchId) {
        List<InventoryDTO> response = inventoryService.getLowStockByBranchId(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Tổng hợp thống kê cảnh báo tồn kho (Dashboard)")
    @GetMapping("/branch/{branchId}/low-stock/summary")
    public ResponseEntity<Map<String, Object>> getLowStockSummary(@PathVariable UUID branchId) {
        Map<String, Object> response = inventoryService.getLowStockSummary(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lấy chi tiết tồn kho của sản phẩm tại một chi nhánh")
    @GetMapping("/product/{productId}/branch/{branchId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductIdAndBranchId(@PathVariable UUID productId, @PathVariable UUID branchId) {
        InventoryDTO response = inventoryService.getInventoryByProductIdAndBranchId(productId, branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lịch sử biến động kho theo chi nhánh (Stock Audit Trail)")
    @GetMapping("/transactions/branch/{branchId}")
    public ResponseEntity<PageResponse<InventoryTransactionDTO>> getTransactionsByBranch(
            @PathVariable UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<InventoryTransactionDTO> response = inventoryService.getTransactionsByBranch(branchId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lịch sử biến động kho của một sản phẩm cụ thể")
    @GetMapping("/transactions/product/{productId}")
    public ResponseEntity<List<InventoryTransactionDTO>> getTransactionsByProduct(@PathVariable UUID productId) {
        List<InventoryTransactionDTO> response = inventoryService.getTransactionsByProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
