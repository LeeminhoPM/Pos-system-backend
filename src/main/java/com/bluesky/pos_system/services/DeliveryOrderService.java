package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.DeliveryStatus;
import com.bluesky.pos_system.payload.dto.CreateOnlineOrderRequestDTO;
import com.bluesky.pos_system.payload.dto.DeliveryOrderDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.UpdateDeliveryStatusRequestDTO;

import java.util.UUID;

public interface DeliveryOrderService {
    DeliveryOrderDTO createOnlineDeliveryOrder(CreateOnlineOrderRequestDTO request);

    DeliveryOrderDTO updateDeliveryStatus(UUID deliveryOrderId, UpdateDeliveryStatusRequestDTO request);

    DeliveryOrderDTO trackDelivery(String trackingNumber);

    DeliveryOrderDTO getDeliveryOrderById(UUID deliveryOrderId);

    PageResponse<DeliveryOrderDTO> getDeliveryOrdersByBranch(UUID branchId, DeliveryStatus status, int page, int size);
}
