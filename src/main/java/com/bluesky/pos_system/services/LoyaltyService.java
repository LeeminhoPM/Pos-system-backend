package com.bluesky.pos_system.services;

import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.*;

import java.util.UUID;

public interface LoyaltyService {
    LoyaltyTransactionDTO earnPoints(Customer customer, Order order, double orderAmount);

    RedeemPointsResponseDTO redeemPoints(RedeemPointsRequestDTO request);

    PageResponse<LoyaltyTransactionDTO> getCustomerLoyaltyHistory(UUID customerId, int page, int size);

    CustomerCRMDetailDTO getCustomerCRMDetail(UUID customerId);

    CustomerSegmentSummaryDTO getCustomerSegmentSummary();
}
