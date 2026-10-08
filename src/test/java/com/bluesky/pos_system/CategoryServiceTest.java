package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.UserService;
import com.bluesky.pos_system.services.impl.CategoryServiceImpl;
import com.bluesky.pos_system.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Store store;
    private Category category;

    @BeforeEach
    void setUp() {
        store = TestDataFactory.createStore();
        category = TestDataFactory.createCategory(store);
    }

    @Test
    @DisplayName("Create Category - Generates Slug Automatically")
    void testCreateCategory_GeneratesSlug() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("Trà Sữa & Topping")
                .description("Các loại trà sữa")
                .storeId(store.getId())
                .build();

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryDTO result = categoryService.createCategory(dto);

        assertNotNull(result);
        assertEquals("Trà Sữa & Topping", result.getName());
        assertNotNull(result.getSlug());
        assertTrue(result.getSlug().contains("tra-sua"));
    }

    @Test
    @DisplayName("Get Category Tree - Returns Hierarchical Root Categories")
    void testGetCategoryTree_Success() {
        Category child = Category.builder()
                .id(UUID.randomUUID())
                .name("Cà Phê Pha Phin")
                .parent(category)
                .store(store)
                .build();
        category.getSubCategories().add(child);

        when(categoryRepository.findByStoreIdAndParentIsNull(store.getId())).thenReturn(List.of(category));

        List<CategoryDTO> tree = categoryService.getCategoryTreeByStore(store.getId());

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("Cà Phê", tree.get(0).getName());
        assertFalse(tree.get(0).getSubCategories().isEmpty());
        assertEquals("Cà Phê Pha Phin", tree.get(0).getSubCategories().get(0).getName());
    }
}
