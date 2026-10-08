package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.mappers.CategoryMapper;
import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.CategoryService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    StoreRepository storeRepository;
    UserService userService;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    @Override
    @CacheEvict(value = {"categories", "categoryTrees"}, allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        User user = null;
        try {
            user = userService.getCurrentUser();
        } catch (Exception ignored) {
        }

        UUID storeId = categoryDTO.getStoreId();
        if (storeId == null && user != null && user.getStore() != null) {
            storeId = user.getStore().getId();
        }

        if (storeId == null) {
            throw new RuntimeException("Cửa hàng không được để trống");
        }

        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy cửa hàng")
        );

        Category parent = null;
        if (categoryDTO.getParentId() != null) {
            parent = categoryRepository.findById(categoryDTO.getParentId()).orElse(null);
        }

        String slug = categoryDTO.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = toSlug(categoryDTO.getName());
        }

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .slug(slug)
                .description(categoryDTO.getDescription())
                .isActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : true)
                .parent(parent)
                .store(store)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDTO(savedCategory);
    }

    @Override
    @Cacheable(value = "categories", key = "#storeId")
    public List<CategoryDTO> getAllCategoriesByStore(UUID storeId) {
        List<Category> categories = categoryRepository.findByStoreId(storeId);
        return categories.stream().map(CategoryMapper::toDTO).toList();
    }

    @Override
    @Cacheable(value = "categoryTrees", key = "#storeId")
    public List<CategoryDTO> getCategoryTreeByStore(UUID storeId) {
        List<Category> rootCategories = categoryRepository.findByStoreIdAndParentIsNull(storeId);
        return rootCategories.stream().map(CategoryMapper::toDTO).toList();
    }

    @Override
    @CacheEvict(value = {"categories", "categoryTrees"}, allEntries = true)
    public CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy danh mục")
        );

        category.setName(categoryDTO.getName());
        if (categoryDTO.getSlug() != null && !categoryDTO.getSlug().isBlank()) {
            category.setSlug(categoryDTO.getSlug());
        } else {
            category.setSlug(toSlug(categoryDTO.getName()));
        }
        category.setDescription(categoryDTO.getDescription());
        if (categoryDTO.getIsActive() != null) {
            category.setIsActive(categoryDTO.getIsActive());
        }
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId()).orElse(null);
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDTO(savedCategory);
    }

    @Override
    @CacheEvict(value = {"categories", "categoryTrees"}, allEntries = true)
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy danh mục")
        );
        categoryRepository.delete(category);
    }
}
