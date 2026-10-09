package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.BranchDTO;

public class BranchMapper {
    public static BranchDTO toDTO (Branch branch) {
        java.util.List<String> days = null;
        try {
            if (branch.getWorkingDays() != null && org.hibernate.Hibernate.isInitialized(branch.getWorkingDays())) {
                days = new java.util.ArrayList<>(branch.getWorkingDays());
            }
        } catch (Exception ignored) {
        }

        return BranchDTO.builder()
                .id(branch.getId())
                .name(branch.getName())
                .phone(branch.getPhone())
                .address(branch.getAddress())
                .email(branch.getEmail())
                .openTime(branch.getOpenTime())
                .closeTime(branch.getCloseTime())
                .workingDays(days)
                .storeId(branch.getStore() != null ? branch.getStore().getId() : null)
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    public static Branch toEntity (BranchDTO dto, Store store) {
        return Branch.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .workingDays(dto.getWorkingDays())
                .store(store)
                .build();
    }
}
