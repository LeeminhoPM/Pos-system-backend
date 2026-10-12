package com.bluesky.pos_system.payload.dto.projection;

public interface DailySalesProjection {
    String getSaleDate();
    Double getTotalRevenue();
    Long getOrderCount();
}
