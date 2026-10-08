package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.CategoryDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> getAllCategoriesByStore(UUID storeId);

    List<CategoryDTO> getCategoryTreeByStore(UUID storeId);

    CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO);

    void deleteCategory(UUID id);
}
