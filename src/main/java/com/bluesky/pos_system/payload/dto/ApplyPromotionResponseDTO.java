package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.DiscountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyPromotionResponseDTO {
    Boolean valid;
    String code;
    String title;
    DiscountType discountType;
    Double discountValue;
    Double discountAmount;
    Double finalAmount;
    String message;
}
