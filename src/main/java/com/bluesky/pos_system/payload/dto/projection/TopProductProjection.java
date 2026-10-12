package com.bluesky.pos_system.payload.dto.projection;

import java.util.UUID;

public interface TopProductProjection {
    UUID getProductId();
    String getProductName();
    String getSku();
    Long getTotalQuantitySold();
    Double getTotalRevenue();
}
