package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.InventoryDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {
    InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO response = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable UUID id, @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO response = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteInventory(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse("Xóa thành công"));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByBranchId(@PathVariable UUID branchId) {
        List<InventoryDTO> response =  inventoryService.getAllInventoryByBranchId(branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product/{productId}/branch/{branchId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductIdAndBranchId(@PathVariable UUID productId, @PathVariable UUID branchId) {
        InventoryDTO response =  inventoryService.getInventoryByProductIdAndBranchId(productId, branchId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
