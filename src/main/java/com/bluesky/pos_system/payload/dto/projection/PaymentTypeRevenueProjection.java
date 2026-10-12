package com.bluesky.pos_system.payload.dto.projection;

import com.bluesky.pos_system.domains.PaymentType;

public interface PaymentTypeRevenueProjection {
    PaymentType getPaymentType();
    Double getTotalRevenue();
    Long getOrderCount();
}
