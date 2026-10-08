package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDTO {
    UUID id;

    String name;

    String slug;

    String description;

    Boolean isActive;

    UUID parentId;

    String parentName;

    List<CategoryDTO> subCategories;

    StoreDTO store;

    UUID storeId;
}
