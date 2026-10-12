package com.bluesky.pos_system.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerSegmentSummaryDTO {
    Long totalCustomers;
    Long bronzeCount;
    Long silverCount;
    Long goldCount;
    Long diamondCount;
    Double totalLoyaltyPointsCirculating;
    Map<String, Long> segmentCounts;
}
