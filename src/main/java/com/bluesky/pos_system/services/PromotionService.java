package com.bluesky.pos_system.services;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ApplyPromotionRequestDTO;
import com.bluesky.pos_system.payload.dto.ApplyPromotionResponseDTO;
import com.bluesky.pos_system.payload.dto.PromotionDTO;

import java.util.List;
import java.util.UUID;

public interface PromotionService {
    PromotionDTO createPromotion(PromotionDTO dto, User user);

    PromotionDTO updatePromotion(UUID id, PromotionDTO dto);

    void deletePromotion(UUID id);

    PromotionDTO getPromotionById(UUID id);

    List<PromotionDTO> getPromotionsByStore(UUID storeId);

    ApplyPromotionResponseDTO applyPromotion(ApplyPromotionRequestDTO request);
}
