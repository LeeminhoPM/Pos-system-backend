package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.ProductDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.InventoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.UserRepository;
import com.bluesky.pos_system.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Store store;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        store = storeRepository.save(TestDataFactory.newStore());
        category = categoryRepository.save(TestDataFactory.newCategory(store));
        product = productRepository.save(TestDataFactory.newProduct(store, category, null));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Success 200 OK")
    @WithMockUser(username = "admin@pos.com", roles = {"ADMIN"})
    void testGetProductById_Success() throws Exception {
        mockMvc.perform(get("/api/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().toString())))
                .andExpect(jsonPath("$.name", is(product.getName())))
                .andExpect(jsonPath("$.sellingPrice", is(product.getSellingPrice())));
    }

    @Test
    @DisplayName("GET /api/products/{id} - 404 Not Found")
    @WithMockUser(username = "admin@pos.com", roles = {"ADMIN"})
    void testGetProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("POST /api/products - Create Product Success 201 Created")
    @WithMockUser(username = "admin@pos.com", roles = {"ADMIN"})
    void testCreateProduct_Success() throws Exception {
        ProductDTO newProduct = ProductDTO.builder()
                .name("Bạc Xỉu Sài Gòn")
                .sku("BX-SG-01")
                .barcode("893999901")
                .costPrice(18000.0)
                .sellingPrice(39000.0)
                .storeId(store.getId())
                .categoryId(category.getId())
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Bạc Xỉu Sài Gòn")))
                .andExpect(jsonPath("$.sku", is("BX-SG-01")))
                .andExpect(jsonPath("$.sellingPrice", is(39000.0)));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Soft Delete Product Success 200 OK")
    @WithMockUser(username = "admin@pos.com", roles = {"ADMIN"})
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }
}
