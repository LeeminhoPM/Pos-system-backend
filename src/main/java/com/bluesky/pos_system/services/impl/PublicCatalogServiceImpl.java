package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.mappers.CategoryMapper;
import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.payload.dto.PublicProductDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.services.PublicCatalogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCatalogServiceImpl implements PublicCatalogService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = "publicCatalog", key = "(#storeId != null ? #storeId.toString() : 'all') + '-' + (#categoryId != null ? #categoryId.toString() : 'all') + '-' + (#keyword != null ? #keyword : '')")
    public List<PublicProductDTO> getPublicCatalog(UUID storeId, UUID categoryId, String keyword) {
        List<Product> products;
        if (storeId != null) {
            products = productRepository.findByStoreId(storeId);
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && !Boolean.TRUE.equals(p.getIsDeleted()))
                .filter(p -> {
                    if (categoryId == null) return true;
                    return p.getCategory() != null && categoryId.equals(p.getCategory().getId());
                })
                .filter(p -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.toLowerCase().trim();
                    return (p.getName() != null && p.getName().toLowerCase().contains(kw)) ||
                           (p.getSku() != null && p.getSku().toLowerCase().contains(kw)) ||
                           (p.getBrand() != null && p.getBrand().toLowerCase().contains(kw));
                })
                .map(this::mapToPublicDTO)
                .toList();
    }

    @Override
    public PublicProductDTO getPublicProductDetail(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + productId));

        if (!Boolean.TRUE.equals(product.getIsActive()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new EntityNotFoundException("Sản phẩm hiện không mở bán");
        }

        return mapToPublicDTO(product);
    }

    @Override
    @Cacheable(value = "publicCategories", key = "#storeId != null ? #storeId.toString() : 'all'")
    public List<CategoryDTO> getPublicCategories(UUID storeId) {
        List<Category> categories;
        if (storeId != null) {
            categories = categoryRepository.findByStoreId(storeId);
        } else {
            categories = categoryRepository.findAll();
        }

        return categories.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .map(CategoryMapper::toDTO)
                .toList();
    }

    private PublicProductDTO mapToPublicDTO(Product p) {
        return PublicProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .brand(p.getBrand())
                .image(p.getImage())
                .description(p.getDescription())
                .sellingPrice(p.getSellingPrice())
                .mrp(p.getMrp())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .inStock(!"OUT_OF_STOCK".equalsIgnoreCase(p.getStatus() != null ? p.getStatus().name() : ""))
                .build();
    }
}
