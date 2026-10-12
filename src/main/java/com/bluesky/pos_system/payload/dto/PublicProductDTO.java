package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;
    String name;
    String sku;
    String barcode;
    String brand;
    String image;
    String description;
    Double sellingPrice;
    Double mrp;
    String categoryName;
    Boolean inStock;
}
