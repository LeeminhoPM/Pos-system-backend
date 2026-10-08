package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.payload.dto.SupplierDTO;

public class SupplierMapper {
    public static SupplierDTO toDTO(Supplier supplier) {
        if (supplier == null) return null;

        return SupplierDTO.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxCode(supplier.getTaxCode())
                .notes(supplier.getNotes())
                .isActive(supplier.getIsActive())
                .storeId(supplier.getStore() != null ? supplier.getStore().getId() : null)
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    public static Supplier toEntity(SupplierDTO dto, Store store) {
        if (dto == null) return null;

        return Supplier.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .contactName(dto.getContactName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .taxCode(dto.getTaxCode())
                .notes(dto.getNotes())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .store(store)
                .build();
    }
}
