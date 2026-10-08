package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;

    String name;

    String sku;

    String barcode;

    String description;

    Double mrp;

    Double costPrice;

    Double sellingPrice;

    Double vatRate;

    String brand;

    String image;

    Integer minStockLevel;

    Boolean isActive;

    ProductStatus status;

    Boolean isDeleted;

    Double profitAmount;

    Double profitMargin;

    CategoryDTO category;

    UUID categoryId;

    SupplierDTO supplier;

    UUID supplierId;

    StoreDTO store;

    UUID storeId;

    Integer currentStock;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
