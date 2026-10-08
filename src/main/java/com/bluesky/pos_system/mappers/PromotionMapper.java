package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Promotion;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.PromotionDTO;

public class PromotionMapper {
    public static PromotionDTO toDTO(Promotion promo) {
        if (promo == null) return null;

        return PromotionDTO.builder()
                .id(promo.getId())
                .code(promo.getCode())
                .title(promo.getTitle())
                .description(promo.getDescription())
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .minOrderValue(promo.getMinOrderValue())
                .maxDiscountAmount(promo.getMaxDiscountAmount())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .usageLimit(promo.getUsageLimit())
                .usedCount(promo.getUsedCount())
                .isActive(promo.getIsActive())
                .storeId(promo.getStore() != null ? promo.getStore().getId() : null)
                .createdAt(promo.getCreatedAt())
                .updatedAt(promo.getUpdatedAt())
                .build();
    }

    public static Promotion toEntity(PromotionDTO dto, Store store) {
        if (dto == null) return null;

        return Promotion.builder()
                .code(dto.getCode() != null ? dto.getCode().toUpperCase().trim() : null)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : 0.0)
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .store(store)
                .build();
    }
}
