package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.CategoryDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {
    public static CategoryDTO toDTO(Category category) {
        if (category == null) return null;

        List<CategoryDTO> subDTOs = null;
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            subDTOs = category.getSubCategories().stream().map(CategoryMapper::toDTO).toList();
        }

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .isActive(category.getIsActive() != null ? category.getIsActive() : true)
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .subCategories(subDTOs != null ? subDTOs : new ArrayList<>())
                .storeId(category.getStore() != null ? category.getStore().getId() : null)
                .build();
    }

    public static Category toEntity(CategoryDTO dto, Store store, Category parent) {
        if (dto == null) return null;

        return Category.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .parent(parent)
                .store(store)
                .build();
    }
}
