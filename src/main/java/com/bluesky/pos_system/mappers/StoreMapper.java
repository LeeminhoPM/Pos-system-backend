package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.StoreDTO;

public class StoreMapper {
    public static StoreDTO toDTO(Store store) {
        if (store == null) return null;
        return StoreDTO.builder()
                .id(store.getId())
                .branch(store.getBranch())
                .storeAdmin(UserMapper.toDTO(store.getStoreAdmin()))
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .description(store.getDescription())
                .storeType(store.getStoreType())
                .status(store.getStatus())
                .contact(store.getContact())
                .build();
    }

    public static Store toEntity(StoreDTO storeDTO, User storeAdmin) {
        return Store.builder()
                .id(storeDTO.getId())
                .branch(storeDTO.getBranch())
                .createdAt(storeDTO.getCreatedAt())
                .updatedAt(storeDTO.getUpdatedAt())
                .description(storeDTO.getDescription())
                .storeType(storeDTO.getStoreType())
                .status(storeDTO.getStatus())
                .contact(storeDTO.getContact())
                .storeAdmin(storeAdmin)
                .build();
    }
}
