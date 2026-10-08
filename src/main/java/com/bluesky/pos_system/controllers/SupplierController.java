package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.SupplierDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.SupplierService;
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
@RequestMapping({"/api/v1/suppliers", "/api/suppliers"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Supplier Management", description = "APIs for managing suppliers and vendor procurement")
public class SupplierController {
    SupplierService supplierService;
    UserService userService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO dto) throws Exception {
        User user = userService.getCurrentUser();
        SupplierDTO response = supplierService.createSupplier(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @Valid @RequestBody SupplierDTO dto) {
        SupplierDTO response = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<ApiResponse> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        ApiResponse res = new ApiResponse();
        res.setMessage("Xóa nhà cung cấp thành công");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier details by ID")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable UUID id) {
        SupplierDTO response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all suppliers for a store")
    public ResponseEntity<List<SupplierDTO>> getSuppliersByStore(@PathVariable UUID storeId) {
        List<SupplierDTO> response = supplierService.getSuppliersByStore(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}/search")
    @Operation(summary = "Search suppliers by keyword")
    public ResponseEntity<List<SupplierDTO>> searchSuppliers(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String query
    ) {
        List<SupplierDTO> response = supplierService.searchSuppliers(storeId, query);
        return ResponseEntity.ok(response);
    }
}
