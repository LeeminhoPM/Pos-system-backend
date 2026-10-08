package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Category Management", description = "APIs for category hierarchy, slug generation, and parent-child trees")
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category with optional parent category")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO response = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get flat list of all categories for a store")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesByStore(@PathVariable UUID storeId) {
        List<CategoryDTO> response = categoryService.getAllCategoriesByStore(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}/tree")
    @Operation(summary = "Get hierarchical category tree (parent-child categories)")
    public ResponseEntity<List<CategoryDTO>> getCategoryTreeByStore(@PathVariable UUID storeId) {
        List<CategoryDTO> response = categoryService.getCategoryTreeByStore(storeId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable UUID id, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO response = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse("Danh mục được xóa thành công"));
    }
}
