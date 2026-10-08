package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Store store;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(TestDataFactory.newStore());
        category = categoryRepository.save(TestDataFactory.newCategory(store));
        product = productRepository.save(TestDataFactory.newProduct(store, category, null));
    }

    @Test
    @DisplayName("findByStoreIdAndIsDeletedFalse - Excludes Soft Deleted Products")
    void testFindByStoreIdAndIsDeletedFalse() {
        Product deletedProduct = TestDataFactory.newProduct(store, category, null);
        deletedProduct.setIsDeleted(true);
        productRepository.save(deletedProduct);

        List<Product> activeProducts = productRepository.findByStoreIdAndIsDeletedFalse(store.getId());

        assertEquals(1, activeProducts.size());
        assertEquals(product.getId(), activeProducts.get(0).getId());
    }

    @Test
    @DisplayName("searchByKeyword - Matches by Name or SKU")
    void testSearchByKeyword() {
        List<Product> matches = productRepository.searchByKeyword(store.getId(), "Cà Phê");

        assertFalse(matches.isEmpty());
        assertEquals(product.getName(), matches.get(0).getName());
    }

    @Test
    @DisplayName("filterProducts - Pagination and Filtering by Status")
    void testFilterProducts_Pagination() {
        Page<Product> page = productRepository.filterProducts(
                store.getId(),
                "Cà Phê",
                category.getId(),
                ProductStatus.IN_STOCK,
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertEquals(product.getId(), page.getContent().get(0).getId());
    }

    @Test
    @DisplayName("findByIdAndIsDeletedFalse - Returns Empty For Deleted Product")
    void testFindByIdAndIsDeletedFalse() {
        product.setIsDeleted(true);
        productRepository.save(product);

        Optional<Product> found = productRepository.findByIdAndIsDeletedFalse(product.getId());

        assertTrue(found.isEmpty());
    }
}
