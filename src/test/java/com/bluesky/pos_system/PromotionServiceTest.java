package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.DiscountType;
import com.bluesky.pos_system.models.Promotion;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.ApplyPromotionRequestDTO;
import com.bluesky.pos_system.payload.dto.ApplyPromotionResponseDTO;
import com.bluesky.pos_system.repositories.PromotionRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.impl.PromotionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion percentagePromo;
    private Promotion expiredPromo;

    @BeforeEach
    void setUp() {
        Store store = Store.builder().id(UUID.randomUUID()).branch("Store 1").build();

        percentagePromo = Promotion.builder()
                .id(UUID.randomUUID())
                .code("SALE20")
                .title("Giảm 20% cho đơn từ 100k")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(20.0)
                .minOrderValue(100000.0)
                .maxDiscountAmount(50000.0)
                .isActive(true)
                .usageLimit(100)
                .usedCount(5)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .store(store)
                .build();

        expiredPromo = Promotion.builder()
                .id(UUID.randomUUID())
                .code("EXPIRED")
                .title("Hết hạn")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(30000.0)
                .isActive(true)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("Apply Promotion - Successful Percentage Calculation capped at maxDiscount")
    void testApplyPromotion_PercentageSuccess() {
        when(promotionRepository.findByCodeIgnoreCase("SALE20")).thenReturn(Optional.of(percentagePromo));

        ApplyPromotionRequestDTO req = ApplyPromotionRequestDTO.builder()
                .code("SALE20")
                .orderAmount(300000.0) // 20% of 300k is 60k, capped at 50k
                .build();

        ApplyPromotionResponseDTO res = promotionService.applyPromotion(req);

        assertTrue(res.getValid());
        assertEquals(50000.0, res.getDiscountAmount());
        assertEquals(250000.0, res.getFinalAmount());
    }

    @Test
    @DisplayName("Apply Promotion - Fails when minimum order requirement not met")
    void testApplyPromotion_MinOrderNotMet() {
        when(promotionRepository.findByCodeIgnoreCase("SALE20")).thenReturn(Optional.of(percentagePromo));

        ApplyPromotionRequestDTO req = ApplyPromotionRequestDTO.builder()
                .code("SALE20")
                .orderAmount(80000.0) // Lower than 100k
                .build();

        ApplyPromotionResponseDTO res = promotionService.applyPromotion(req);

        assertFalse(res.getValid());
        assertEquals(0.0, res.getDiscountAmount());
        assertEquals(80000.0, res.getFinalAmount());
        assertTrue(res.getMessage().contains("tối thiểu"));
    }

    @Test
    @DisplayName("Apply Promotion - Fails when coupon has expired")
    void testApplyPromotion_Expired() {
        when(promotionRepository.findByCodeIgnoreCase("EXPIRED")).thenReturn(Optional.of(expiredPromo));

        ApplyPromotionRequestDTO req = ApplyPromotionRequestDTO.builder()
                .code("EXPIRED")
                .orderAmount(150000.0)
                .build();

        ApplyPromotionResponseDTO res = promotionService.applyPromotion(req);

        assertFalse(res.getValid());
        assertTrue(res.getMessage().contains("hết hạn"));
    }
}
