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
public class CategoryDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;

    @jakarta.validation.constraints.NotBlank(message = "Tên danh mục không được để trống")
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
