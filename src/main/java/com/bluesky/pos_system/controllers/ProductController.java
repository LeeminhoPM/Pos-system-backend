package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.ProductDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Product Management", description = "APIs for product catalog, search, pagination, barcode/SKU, and profit calculation")
public class ProductController {
    ProductService productService;
    UserService userService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User user = null;
        try {
            user = (token != null) ? userService.getUserFromJwtToken(token) : userService.getCurrentUser();
        } catch (Exception ignored) {
        }
        ProductDTO response = productService.createProduct(productDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        ProductDTO response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{id}")
    @Operation(summary = "Get all products by store ID")
    public ResponseEntity<List<ProductDTO>> getProductByStoreId(@PathVariable UUID id) {
        List<ProductDTO> response = productService.getAllProductsByStoreId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{id}/paged")
    @Operation(summary = "Get products by store with pagination, search, category, and status filters")
    public ResponseEntity<PageResponse<ProductDTO>> getProductsPaged(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status
    ) {
        PageResponse<ProductDTO> response = productService.getProductsPaged(id, page, size, keyword, categoryId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{id}/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<List<ProductDTO>> getProductByKeyword(@PathVariable UUID id, @RequestParam String keyword) {
        List<ProductDTO> response = productService.searchByKeyword(id, keyword);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDTO productDTO,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User user = null;
        try {
            user = (token != null) ? userService.getUserFromJwtToken(token) : userService.getCurrentUser();
        } catch (Exception ignored) {
        }
        ProductDTO response = productService.updateProduct(id, productDTO, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a product")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User user = null;
        try {
            user = (token != null) ? userService.getUserFromJwtToken(token) : userService.getCurrentUser();
        } catch (Exception ignored) {
        }
        productService.deleteProduct(id, user);
        return ResponseEntity.ok(
                ApiResponse.builder().message("Xóa sản phẩm thành công").build()
        );
    }
}
