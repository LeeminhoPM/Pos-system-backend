package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.payload.dto.ProductDTO;

public class ProductMapper {
    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

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
                .supplier(product.getSupplier() != null ? SupplierMapper.toDTO(product.getSupplier()) : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .store(product.getStore() != null ? StoreMapper.toDTO(product.getStore()) : null)
                .category(product.getCategory() != null ? CategoryMapper.toDTO(product.getCategory()) : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .storeId(product.getStore() != null ? product.getStore().getId() : null)
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
