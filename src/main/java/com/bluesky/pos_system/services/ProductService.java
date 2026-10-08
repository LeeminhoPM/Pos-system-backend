package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO, User user);

    ProductDTO updateProduct(UUID id, ProductDTO productDTO, User user);

    void deleteProduct(UUID id, User user);

    void softDeleteProduct(UUID id, User user);

    ProductDTO getProductById(UUID id);

    List<ProductDTO> getAllProductsByStoreId(UUID storeId);

    PageResponse<ProductDTO> getProductsPaged(UUID storeId, int page, int size, String keyword, UUID categoryId, ProductStatus status);

    List<ProductDTO> searchByKeyword(UUID storeId, String keyword);
}
