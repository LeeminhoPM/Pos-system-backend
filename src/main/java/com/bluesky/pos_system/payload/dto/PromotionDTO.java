package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionDTO {
    UUID id;

    @NotBlank(message = "Mã khuyến mãi là bắt buộc")
    String code;

    @NotBlank(message = "Tiêu đề chương trình là bắt buộc")
    String title;

    String description;

    @NotNull(message = "Loại giảm giá là bắt buộc (PERCENTAGE hoặc FIXED_AMOUNT)")
    DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá là bắt buộc")
    Double discountValue;

    Double minOrderValue;

    Double maxDiscountAmount;

    LocalDateTime startDate;

    LocalDateTime endDate;

    Integer usageLimit;

    Integer usedCount;

    Boolean isActive;

    UUID storeId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
