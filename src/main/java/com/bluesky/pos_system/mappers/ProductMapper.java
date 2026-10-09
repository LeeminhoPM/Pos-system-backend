package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.payload.dto.ProductDTO;
import java.util.UUID;

public class ProductMapper {
    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        com.bluesky.pos_system.payload.dto.SupplierDTO supplierDTO = null;
        UUID supplierId = null;
        try {
            if (product.getSupplier() != null) {
                supplierId = product.getSupplier().getId();
                supplierDTO = SupplierMapper.toDTO(product.getSupplier());
            }
        } catch (Exception ignored) {}

        com.bluesky.pos_system.payload.dto.StoreDTO storeDTO = null;
        UUID storeId = null;
        try {
            if (product.getStore() != null) {
                storeId = product.getStore().getId();
                storeDTO = StoreMapper.toDTO(product.getStore());
            }
        } catch (Exception ignored) {}

        com.bluesky.pos_system.payload.dto.CategoryDTO categoryDTO = null;
        UUID categoryId = null;
        try {
            if (product.getCategory() != null) {
                categoryId = product.getCategory().getId();
                categoryDTO = CategoryMapper.toDTO(product.getCategory());
            }
        } catch (Exception ignored) {}

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .sku(product.getSku())
                .barcode(product.getBarcode() != null ? product.getBarcode() : product.getSku())
                .description(product.getDescription())
                .mrp(product.getMrp())
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .vatRate(product.getVatRate() != null ? product.getVatRate() : 0.08)
                .image(product.getImage())
                .minStockLevel(product.getMinStockLevel())
                .isActive(product.getIsActive() != null ? product.getIsActive() : true)
                .status(product.getStatus())
                .isDeleted(product.getIsDeleted() != null ? product.getIsDeleted() : false)
                .profitAmount(product.getProfitAmount())
                .profitMargin(product.getProfitMargin())
                .supplier(supplierDTO)
                .supplierId(supplierId)
                .store(storeDTO)
                .category(categoryDTO)
                .categoryId(categoryId)
                .storeId(storeId)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static ProductDTO toDTO(Product product, Integer currentStock) {
        ProductDTO dto = toDTO(product);
        if (dto != null && currentStock != null) {
            dto.setCurrentStock(currentStock);
        }
        return dto;
    }

    public static Product toEntity(ProductDTO productDTO, Store store, Category category, Supplier supplier) {
        if (productDTO == null) return null;

        return Product.builder()
                .name(productDTO.getName())
                .brand(productDTO.getBrand())
                .sku(productDTO.getSku())
                .barcode(productDTO.getBarcode() != null ? productDTO.getBarcode() : productDTO.getSku())
                .description(productDTO.getDescription())
                .mrp(productDTO.getMrp())
                .costPrice(productDTO.getCostPrice())
                .sellingPrice(productDTO.getSellingPrice())
                .vatRate(productDTO.getVatRate() != null ? productDTO.getVatRate() : 0.08)
                .image(productDTO.getImage())
                .minStockLevel(productDTO.getMinStockLevel() != null ? productDTO.getMinStockLevel() : 5)
                .isActive(productDTO.getIsActive() != null ? productDTO.getIsActive() : true)
                .status(productDTO.getStatus())
                .isDeleted(false)
                .category(category)
                .supplier(supplier)
                .store(store)
                .build();
    }
}
