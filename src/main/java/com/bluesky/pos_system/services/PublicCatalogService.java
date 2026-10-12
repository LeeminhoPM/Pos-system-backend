package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.payload.dto.PublicProductDTO;

import java.util.List;
import java.util.UUID;

public interface PublicCatalogService {
    List<PublicProductDTO> getPublicCatalog(UUID storeId, UUID categoryId, String keyword);

    PublicProductDTO getPublicProductDetail(UUID productId);

    List<CategoryDTO> getPublicCategories(UUID storeId);
}
