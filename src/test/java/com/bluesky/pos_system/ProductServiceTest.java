package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ProductDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.SupplierRepository;
import com.bluesky.pos_system.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private com.bluesky.pos_system.repositories.InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID storeId;
    private Store store;
    private Product product;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        store = Store.builder().id(storeId).branch("POS Flagship Store").build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Cà phê sữa đá")
                .sku("CFS-001")
                .barcode("8930000001")
                .costPrice(15000.0)
                .sellingPrice(35000.0)
                .vatRate(0.08)
                .status(ProductStatus.IN_STOCK)
                .isDeleted(false)
                .store(store)
                .build();
    }

    @Test
    @DisplayName("Create Product - Success with Auto Barcode and SKU")
    void testCreateProduct_Success() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Đồ uống").build();

        ProductDTO request = ProductDTO.builder()
                .name("Trà đào cam sả")
                .costPrice(18000.0)
                .sellingPrice(45000.0)
                .storeId(storeId)
                .categoryId(categoryId)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        User user = User.builder().store(store).build();
        ProductDTO created = productService.createProduct(request, user);

        assertNotNull(created);
        assertEquals("Trà đào cam sả", created.getName());
        assertEquals(45000.0, created.getSellingPrice());
        assertNotNull(created.getSku());
        assertTrue(created.getSku().startsWith("SKU-"));
        assertEquals(27000.0, created.getProfitAmount());
        assertEquals(60.0, created.getProfitMargin(), 0.01);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Soft Delete Product - Sets isDeleted to true")
    void testSoftDeleteProduct_Success() {
        UUID productId = product.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.softDeleteProduct(productId, null);

        assertTrue(product.getIsDeleted());
        assertFalse(product.getIsActive());
        assertNotNull(product.getDeletedAt());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Get Product By ID - Returns Correct DTO")
    void testGetProductById_Success() {
        when(productRepository.findByIdAndIsDeletedFalse(product.getId())).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(product.getId());

        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals("Cà phê sữa đá", result.getName());
        assertEquals(20000.0, result.getProfitAmount());
    }
}
