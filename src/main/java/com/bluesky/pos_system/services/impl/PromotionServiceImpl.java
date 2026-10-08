package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.DiscountType;
import com.bluesky.pos_system.mappers.PromotionMapper;
import com.bluesky.pos_system.models.Promotion;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ApplyPromotionRequestDTO;
import com.bluesky.pos_system.payload.dto.ApplyPromotionResponseDTO;
import com.bluesky.pos_system.payload.dto.PromotionDTO;
import com.bluesky.pos_system.repositories.PromotionRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.PromotionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionServiceImpl implements PromotionService {
    PromotionRepository promotionRepository;
    StoreRepository storeRepository;

    @Override
    public PromotionDTO createPromotion(PromotionDTO dto, User user) {
        UUID storeId = dto.getStoreId();
        if (storeId == null && user != null && user.getStore() != null) {
            storeId = user.getStore().getId();
        }
        if (storeId == null) {
            throw new RuntimeException("Cửa hàng không được để trống");
        }

        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy cửa hàng")
        );

        Promotion promo = PromotionMapper.toEntity(dto, store);
        return PromotionMapper.toDTO(promotionRepository.save(promo));
    }

    @Override
    public PromotionDTO updatePromotion(UUID id, PromotionDTO dto) {
        Promotion promo = promotionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy mã khuyến mãi")
        );

        promo.setTitle(dto.getTitle());
        promo.setDescription(dto.getDescription());
        if (dto.getDiscountType() != null) {
            promo.setDiscountType(dto.getDiscountType());
        }
        if (dto.getDiscountValue() != null) {
            promo.setDiscountValue(dto.getDiscountValue());
        }
        promo.setMinOrderValue(dto.getMinOrderValue());
        promo.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());
        promo.setUsageLimit(dto.getUsageLimit());
        if (dto.getIsActive() != null) {
            promo.setIsActive(dto.getIsActive());
        }

        return PromotionMapper.toDTO(promotionRepository.save(promo));
    }

    @Override
    public void deletePromotion(UUID id) {
        Promotion promo = promotionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy mã khuyến mãi")
        );
        promotionRepository.delete(promo);
    }

    @Override
    public PromotionDTO getPromotionById(UUID id) {
        Promotion promo = promotionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy mã khuyến mãi")
        );
        return PromotionMapper.toDTO(promo);
    }

    @Override
    public List<PromotionDTO> getPromotionsByStore(UUID storeId) {
        return promotionRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
                .map(PromotionMapper::toDTO)
                .toList();
    }

    @Override
    public ApplyPromotionResponseDTO applyPromotion(ApplyPromotionRequestDTO request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi không được để trống")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        Optional<Promotion> promoOpt = promotionRepository.findByCodeIgnoreCase(request.getCode().trim());
        if (promoOpt.isEmpty()) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi '" + request.getCode() + "' không tồn tại")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        Promotion promo = promoOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.FALSE.equals(promo.getIsActive())) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi đã ngưng hoạt động")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        if (promo.getStartDate() != null && promo.getStartDate().isAfter(now)) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi chưa có hiệu lực")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        if (promo.getEndDate() != null && promo.getEndDate().isBefore(now)) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi đã hết hạn")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Mã khuyến mãi đã hết lượt sử dụng")
                    .discountAmount(0.0)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }

        double orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : 0.0;
        if (promo.getMinOrderValue() != null && orderAmount < promo.getMinOrderValue()) {
            return ApplyPromotionResponseDTO.builder()
                    .valid(false)
                    .message("Đơn hàng tối thiểu phải từ " + String.format("%,.0f", promo.getMinOrderValue()) + " đ để áp dụng mã")
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        double discount = 0.0;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount * (promo.getDiscountValue() / 100.0);
            if (promo.getMaxDiscountAmount() != null && discount > promo.getMaxDiscountAmount()) {
                discount = promo.getMaxDiscountAmount();
            }
        } else {
            discount = promo.getDiscountValue();
        }

        discount = Math.min(discount, orderAmount);
        double finalAmount = Math.max(0.0, orderAmount - discount);

        return ApplyPromotionResponseDTO.builder()
                .valid(true)
                .code(promo.getCode())
                .message("Áp dụng mã khuyến mãi thành công!")
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .build();
    }
}
